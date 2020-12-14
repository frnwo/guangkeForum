package com.guangke.forum.event;
import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.MessageService;
import com.guangke.forum.service.SearchService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements ForumConstants {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //一旦生产者线程向队列中放了event(被包装成ConsumerRecord),消费者线程就会取出event,并添加到数据库的message表
    @Autowired
    MessageService messageService;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    SearchService searchService;

    @Value("${wk.cmd}")
    private String wkCommand;

    @Value("${wk.images.storage}")
    private String wkStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;
    /**
     *  定时任务使用Spring的，而不是Quartz的分布式定时任务
     *  因为多个应用服务器同时存在着相同的定时任务，就可能任务重复执行，比如
     *  每个应用服务器都有更新帖子分数的定时任务，服务器A并不知道服务器B正在执行
     *  或刚刚完成该定时任务，所以为了避免重复执行，就把任务信息存到数据库，而数据库
     *  是唯一的，服务器B取出该任务时，数据库将该任务的状态更改为正在执行，这样
     *  服务器A 就不能执行了
     *  所以更新帖子分数的定时任务才需要Quartz
     *
     *  当有生成长图的事件时，使用定时任务每隔一段时间查看wk这个命令是否已经生成了长图，
     *  检查是否成功发布到七牛云，这个任务虽然每个服务器都会有，但与更新帖子分数的区别是：
     *  多了kafka消息队列！服务器B从kafka服务器获取分享消息后开启了这个生成长图的任务，
     *  服务器A并没有获取这个消息，所以不会开启这个任务，因此就不用Quartz
     */
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentEvent(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容不能为空！");
            return;
        }

        //record.value().toString()是一个json格式字符串，转换为指定的Event对象
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if(event == null){
            logger.error("消息格式错误！");
            return;
        }

        Message message = new Message();
        message.setFromId(SYSTEM_USER);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        //content字段是固定的三个数据,实体类型+实体id+实体所属的用户id,再加上扩展的数据
        Map<String,Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId",event.getEntityId());
        //content字段的扩展数据
        if(!event.getData().isEmpty()){
            for(Map.Entry<String,Object> entry : event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));

        messageService.addMessage(message);
    }

    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublicMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容不能为空！");
            return;
        }

        //record.value().toString()是一个json格式字符串，转换为指定的Event对象
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        //根据id找到帖子
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        //保存帖子到es服务器
        searchService.save(post);

    }


    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteDiscussPost(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容不能为空！");
            return;
        }

        //record.value().toString()是一个json格式字符串，转换为指定的Event对象
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        //从es服务器删除帖子
        searchService.delete(event.getEntityId());

    }

    //消费分享event
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record){
        if(record == null || record.value() == null){
            logger.error("消息内容不能为空！");
            return;
        }

        //record.value().toString()是一个json格式字符串，转换为指定的Event对象
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);

        if(event == null){
            logger.error("消息格式错误！");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd  = wkCommand + htmlUrl + " "+wkStorage+"/"+fileName+"."+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功 "+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败 "+e.getMessage());
        }

        //使用定时任务每个半秒检查是否真的成功生成了长图，并将它上传到七牛云，如果30秒钟内还没上传
        //到七牛云或者上传三次都失败了就取消上传
        UploadTask task = new UploadTask(fileName,suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task,500);
        task.setFuture(future);
    }


    class UploadTask implements Runnable{
        //文件名
        private String fileName;
        //文件后缀
        private String suffix;
        //任务的返回值
        private Future future;
        //任务的开始时间
        private long startTime;
        //上传次数
        private int uploadTimes;


        private void setFuture(Future future){
            this.future = future;
        }
        public UploadTask(String fileName, String suffix) {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void run() {
            if(System.currentTimeMillis() - startTime > 30* 1000){
                logger.error("执行时间大于30秒，任务取消: "+fileName);
                future.cancel(true);
                return;
            }

            if(uploadTimes >= 3){
                logger.error("上传次数大于3次，任务取消："+fileName);
                future.cancel(true);
                return;
            }

            String path = wkStorage+"/"+fileName+"."+suffix;
            File file = new File(path);
            if(file.exists()){
                //图片已经生成，上传到七牛云
                logger.info(String.format("开始第[%d]次上传[%s]",++uploadTimes,fileName));
                //设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", ForumUtils.getJSONString(0));
                //生成上传凭证
                Auth auth = Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName,fileName,3600,policy);
                //指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone2()));
                try{
                    //开始上传图片
                    Response response = manager.put(path,fileName,uploadToken,null,"image/"+suffix,false);
                    //处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if(json == null || json.get("code")== null || !json.get("code").toString().equals("0")){
                        logger.info(String.format("第[%d]次上传失败[%s]",uploadTimes,fileName));
                    }else{
                        logger.info(String.format("第[%d]次上传成功[%s]",uploadTimes,fileName));
                        future.cancel(true);
                    }
                }catch(QiniuException e){
                    logger.info(String.format("第[%d]次上传失败[%s]",uploadTimes,fileName));
                }
            }else {
                logger.info("等待图片生成["+fileName+"]");
            }

        }
    }
}


package com.guangke.forum.event;
import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.MessageService;
import com.guangke.forum.service.SearchService;
import com.guangke.forum.util.ForumConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        String cmd  = wkCommand + " --quality 75 "+ htmlUrl + " "+wkStorage+"/"+fileName+suffix;
        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功 "+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败 "+e.getMessage());
        }

    }
}

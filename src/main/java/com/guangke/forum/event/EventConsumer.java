package com.guangke.forum.event;
import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.pojo.Message;
import com.guangke.forum.service.MessageService;
import com.guangke.forum.util.ForumConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements ForumConstants {

    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    //一旦生产者线程向队列中放了event(被包装成ConsumerRecord),消费者线程就会取出event,并添加到数据库的message表
    @Autowired
    MessageService messageService;

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
}

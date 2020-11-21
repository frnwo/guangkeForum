package com.guangke.forum.event;

import com.alibaba.fastjson.JSONObject;
import com.guangke.forum.pojo.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class EventProducer {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    public void fireEvent(String topic, Event event){
        kafkaTemplate.send(topic, JSONObject.toJSONString(event));
    }
}

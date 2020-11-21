package com.guangke.forum.pojo;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息表的实体类
 */
public class Event {

    //主题，存到message表的conversation_id字段
    private String topic;

    //接收该事件的用户，message表的to_id
    private int entityUserId;

    //事件的实体类型，比如说是对帖子点赞还是评论点赞 ，message表没有的字段，添加到content字段
    private int entityType;

    //事件的实体ID，比如帖子id或者评论id 等等， message表没有的字段，添加到content字段
    private int entityId;

    //哪个用户触发的事件,比如哪个用户对你进行了点赞、评论、关注，message表没有的字段，添加到content字段
    private int userId;


    /**
     *  当需要显示其他字段时，往mpa中put()，然后消费者线程取出来，再往content字段中追加
     */
    private Map<String,Object> data = new HashMap<>();

    public String getTopic() {
        return topic;
    }

    public Event setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public int getUserId() {
        return userId;
    }

    public Event setUserId(int userId) {
        this.userId = userId;
        return this;
    }

    public int getEntityUserId() {
        return entityUserId;
    }

    public Event setEntityUserId(int entityUserId) {
        this.entityUserId = entityUserId;
        return this;
    }

    public int getEntityType() {
        return entityType;
    }

    public Event setEntityType(int entityType) {
        this.entityType = entityType;
        return this;
    }

    public int getEntityId() {
        return entityId;
    }

    public Event setEntityId(int entityId) {
        this.entityId = entityId;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public Event setData(String key,Object value) {
        this.data.put(key,value);
        return this;
    }
}

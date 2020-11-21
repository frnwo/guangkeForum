package com.guangke.forum.pojo;

import java.util.Date;

public class Message {
    private int id;
    //1：来自系统
    private int fromId;
    private int toId;
    private String content;
    /**
     *   用户的消息有发送也有接收，只要有一方是用户id,就是用户的会话，
     *   因此用此字段表示，方便以后查询
     */
    private String conversationId;
    private int status;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public int getToId() {
        return toId;
    }

    public void setToId(int toId) {
        this.toId = toId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", from_id=" + fromId +
                ", to_id=" + toId +
                ", content='" + content + '\'' +
                ", conversation_id='" + conversationId + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                '}';
    }
}

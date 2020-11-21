package com.guangke.forum.pojo;

import java.util.Date;

/**
 * 评论: entity_type 为 1 ，entity_id为帖子的id,通过这两个条件查询出帖子的comment
 * 回复: entity_type 为 2 ,entity_id为评论的id,通过这两个条件查询出评论的comment
 */
public class Comment {
    private int id;
    //哪个用户(userId)评论了帖子或者哪个用户(userId)回复了评论
    private int userId;
    /**
     *  数据库中的comment表的每一行可以是对帖子的评论或在评论下的回复，
     *  这两种评论用entity_type来区别
     */
    private int entityType;

    private int entityId;

    /**
     *         如果comment是回复类型的话，根据targetId值可分为两种类型：0 表示直接回复， 否则 表示回复是针对targetId用户
     */
    private int targetId;

    //0：正常
    private int status;

    private String content;

    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEntityType() {
        return entityType;
    }

    public void setEntityType(int entityType) {
        this.entityType = entityType;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", userId=" + userId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", targetId=" + targetId +
                ", status=" + status +
                ", content='" + content + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}

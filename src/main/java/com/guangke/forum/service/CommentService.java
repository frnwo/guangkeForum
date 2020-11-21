package com.guangke.forum.service;

import com.guangke.forum.mapper.CommentMapper;
import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.pojo.Comment;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class CommentService implements ForumConstants {

    @Autowired
    CommentMapper commentMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    @Autowired
    DiscussPostService discussPostService;

    public List<Comment> getCommentList(int entityType,int entityId,int offset,int limit){
        return commentMapper.selectCommentByEntity(entityType,entityId,offset,limit);
    }

    public int getCommentCount(int entityType,int entityId){
        return commentMapper.selectCommentCountByEntity(entityType,entityId);
    }

    /**
     * 增加评论后再跟新回帖量需要事务管理
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public int addComment(Comment comment){
        if(comment == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义+过滤敏感词
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);
        //更新帖子的评论数量
        if(comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST){
            int count = commentMapper.selectCommentCountByEntity(ENTITY_TYPE_DISCUSSPOST,comment.getEntityId());
            discussPostService.updateCommentCount(comment.getEntityId(),count);
        }
        return rows;
    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }
}

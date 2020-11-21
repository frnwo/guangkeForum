package com.guangke.forum.controller;

import com.guangke.forum.event.EventProducer;
import com.guangke.forum.pojo.Comment;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.service.CommentService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Date;

@Controller
@RequestMapping("/comment")
public class CommentController implements ForumConstants {

    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    EventProducer producer;

    //添加完评论之后，需要重定向到帖子详情，因此需要传进来一个帖子id
    @PostMapping("/add/{postId}")
    public String addComment(@PathVariable int postId, Comment comment){
        comment.setCreateTime(new Date());
        comment.setStatus(0);
        comment.setUserId(hostHolder.get().getId());
        commentService.addComment(comment);

        Integer  entityUserId = null;
        if (comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST) {
            DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
            entityUserId = post.getUserId();
        } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
            Comment comment1 = commentService.findCommentById(comment.getEntityId());
            entityUserId = comment1.getUserId();
        }

        //触发评论事件,
        if(hostHolder.get().getId() != entityUserId) {

            Event event = new Event()
                    .setTopic(TOPIC_COMMENT)
                    .setEntityType(comment.getEntityType())
                    .setEntityId(comment.getEntityId())
                    .setUserId(hostHolder.get().getId())
                    .setData("postId", postId); //评论通知需要跳转到相应的post

            //评论是针对帖子或者评论的，所属者userId要根据entityType类型来查询
//            if (comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST) {
//                //根据帖子id找出帖子，得到userId
//                DiscussPost post = discussPostService.findDiscussPostById(comment.getEntityId());
//                event.setEntityUserId(post.getUserId());
//            } else if (comment.getEntityType() == ENTITY_TYPE_COMMENT) {
//                Comment comment1 = commentService.findCommentById(comment.getEntityId());
//                event.setEntityUserId(comment1.getUserId());
//            }
            event.setEntityUserId(entityUserId);

            producer.fireEvent(TOPIC_COMMENT, event);
        }
        return "redirect:/discuss/detail/"+postId;
    }

}

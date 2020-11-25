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
import com.guangke.forum.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

    @Autowired
    private RedisTemplate redisTemplate;

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
                    //评论通知需要跳转到相应的post
                    .setData("postId", postId)
                    .setEntityUserId(entityUserId);

            producer.fireEvent(TOPIC_COMMENT, event);
        }

        /**
         *  增加评论后，帖子的回复数量发生改变，es服务器的帖子也需要更新
         *  如果评论针对的实体类型(entityType)是帖子，则触发事件
         *  消费者线程将更新的帖子保存到es服务器
         */
        if(comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST){
            Event event = new Event()
                    .setTopic(TOPIC_PUBLISH)
                    .setUserId(comment.getUserId())
                    .setEntityType(ENTITY_TYPE_DISCUSSPOST)
                    .setEntityId(postId);

            producer.fireEvent(TOPIC_PUBLISH,event);

            //对帖子评论也要更新帖子的分数
            String redisKey = RedisKeyUtils.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);

        }

        return "redirect:/discuss/detail/"+postId;
    }

}

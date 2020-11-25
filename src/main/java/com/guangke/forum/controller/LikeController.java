package com.guangke.forum.controller;

import com.guangke.forum.event.EventProducer;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import com.guangke.forum.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements ForumConstants {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    LikeService likeService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    //异步 点赞
    @PostMapping(path = "/like")
    @ResponseBody
    public String like(int entityType,int entityId,int entityUserId,int postId){
        Map<String,Object> map = new HashMap<>();
        User user = hostHolder.get();
        //点赞
        likeService.like(user.getId(),entityType,entityId,entityUserId);
        //点赞数量
        long likeCount = likeService.findLikeCount(entityType,entityId);
        map.put("likeCount",likeCount);
        //点赞状态
        int likeStatus = user==null ? 0 : likeService.findLikeStatus(user.getId(),entityType,entityId);
        map.put("likeStatus",likeStatus);

        //触发点赞,自己给自己点赞不会通知
        if(entityUserId != user.getId() && likeStatus == 1){
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.get().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId",postId); //无论给帖子还是评论点赞，在显示通知时都要给出一个帖子的链接(postId)

            eventProducer.fireEvent(TOPIC_LIKE,event);
        }
        //对帖子点赞要更新帖子的分数
        if(entityType == ENTITY_TYPE_DISCUSSPOST){
            String redisKey = RedisKeyUtils.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey,postId);
        }


        return ForumUtils.getJSONString(0,null,map);
    }
}

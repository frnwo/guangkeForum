package com.guangke.forum.controller;

import com.guangke.forum.event.EventProducer;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.pojo.Page;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.FollowService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class FollowController implements ForumConstants {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private EventProducer eventProducer;

    @PostMapping(path = "/follow")
    @ResponseBody
    public String follow(int entityType,int entityId){
        User user = hostHolder.get();
        followService.follow(user.getId(),entityType,entityId);

        //触发关注事件
        Event event = new Event()
                .setTopic(TOPIC_FOLLOW)
                .setUserId(hostHolder.get().getId())
                .setEntityType(entityType)
                .setEntityId(entityId)
                .setEntityUserId(entityId);
        eventProducer.fireEvent(TOPIC_FOLLOW,event);

        return ForumUtils.getJSONString(0,"已关注!");
    }

    @PostMapping(path = "/unfollow")
    @ResponseBody
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.get();
        followService.unfollow(user.getId(),entityType,entityId);
        return ForumUtils.getJSONString(0,"已取消关注!");
    }

    @GetMapping(path = "/followees/{userId}")
    public String getFollowees(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);
        page.setRows((int) followService.getFolloweeCount(userId,ENTITY_TYPE_USER));
        page.setPath("/followees/"+userId);
        page.setLimit(5);

        List<Map<String,Object>> userList = followService.findFollowees(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hostHolder.get() == null?false:followService.hasFollowed(hostHolder.get().getId(),ENTITY_TYPE_USER,u.getId()));
            }
        }

        model.addAttribute("users",userList);
        return "/site/followee";
    }

    @GetMapping(path = "/followers/{userId}")
    public String getFollowers(@PathVariable("userId") int userId, Model model, Page page){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }
        model.addAttribute("user",user);

        page.setRows((int) followService.getFollowerCount(ENTITY_TYPE_USER,userId));
        page.setPath("/followers/"+userId);
        page.setLimit(5);

        List<Map<String,Object>> userList = followService.findFollowers(userId,page.getOffset(),page.getLimit());
        if(userList != null){
            for(Map<String,Object> map : userList){
                User u = (User) map.get("user");
                map.put("hasFollowed",hostHolder.get() == null?false:followService.hasFollowed(hostHolder.get().getId(),ENTITY_TYPE_USER,u.getId()));
            }
        }
        model.addAttribute("users",userList);
        return "/site/follower";
    }
}

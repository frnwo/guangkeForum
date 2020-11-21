package com.guangke.forum.controller;

import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Page;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;

@Controller
public class HomeController implements ForumConstants {
    @Autowired
    UserService userService;
    @Autowired
    DiscussPostService discussPostService;

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @GetMapping(path = "/index")
    public String getIndex(Model model, Page page){
        // 方法调用前,SpringMVC会自动实例化Model和Page,并将Page注入Model.
        // 所以,在thymeleaf中可以直接访问Page对象中的数据.
        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index");

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit());
        List<Map<String,Object>> discussPosts = new ArrayList<>();
        for(DiscussPost post : list){
            Map map = new HashMap();
            map.put("post",post);

            //帖子的作者
            User user = userService.findUserById(post.getUserId());
            map.put("user",user);

            //帖子的点赞数量
            map.put("likeCount",likeService.findLikeCount(ENTITY_TYPE_DISCUSSPOST,post.getId()));

            //当前用户对帖子点赞状态
            int likeStatus = hostHolder.get()==null?0:likeService.findLikeStatus(hostHolder.get().getId(),ENTITY_TYPE_DISCUSSPOST,post.getId());
            map.put("likeStatus",likeStatus);

            discussPosts.add(map);
        }
        model.addAttribute("discussPosts",discussPosts);
        return "/index";
    }
    @GetMapping("/errorPage")
    public String getErrorPage(){
        return "/error/500";
    }
}

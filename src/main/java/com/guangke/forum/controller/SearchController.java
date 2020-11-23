package com.guangke.forum.controller;

import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.Page;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.service.SearchService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements ForumConstants {
    @Autowired
    SearchService searchService;

    @Autowired
    UserService userService;

    @Autowired
    LikeService likeService;

    //search?keyword=
    @GetMapping(path = "/search")
    public String search(String keyword,Model model, Page page){

        //搜索帖子，es的current从0开始，而不是1
        org.springframework.data.domain.Page<DiscussPost> searchResult =
                searchService.searchDiscussPost(keyword,page.getCurrent() - 1,page.getLimit());

        //聚合数据
        List<Map<String,Object>> postList = new ArrayList<>();

        if(searchResult != null){
            for(DiscussPost post : searchResult){
                Map<String,Object> map = new HashMap<>();
                //帖子
                map.put("post",post);
                //作者
                User user  = userService.findUserById(post.getUserId());
                map.put("user",user);
                //点赞数
                int count = (int) likeService.findLikeCount(ENTITY_TYPE_DISCUSSPOST,post.getId());
                map.put("likeCount",count);

                postList.add(map);
            }
        }

        model.addAttribute("posts",postList);

        //返回搜索结果页面需要把关键词带上
        model.addAttribute("keyword",keyword);

        //分页信息
        page.setPath("/search?keyword="+keyword);
        page.setRows(searchResult == null ? 0 : (int) searchResult.getTotalElements());
        return "/site/search";
    }
}

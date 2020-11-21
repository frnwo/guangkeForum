package com.guangke.forum.service;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AlpahService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    DiscussPostMapper discussPostMapper;

    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED)
    public Object addUserAndPost(){
        //增加用户
        User user = new User();
        user.setUsername("2");
        user.setPassword("2");
        user.setHeaderUrl("2");
        user.setSalt("2");
        user.setActivationCode("2");
        user.setCreateTime(new Date());
        userMapper.insertUser(user);
        //增加帖子
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle("test2");
        post.setContent("test2");
        post.setCreateTime(new Date());
        discussPostMapper.insertDiscussPost(post);
        Integer.valueOf("abc");
        return "ok";

    }
}


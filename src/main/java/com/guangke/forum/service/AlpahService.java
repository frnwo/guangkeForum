package com.guangke.forum.service;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class AlpahService {
    private Logger logger = LoggerFactory.getLogger(AlpahService.class);
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
    @Async
    public void execute1(){
        logger.debug("execute1");
    }
    //只要程序启动，就会自动执行
//    @Scheduled(initialDelay = 5000,fixedRate = 1000)
    public void execute2(){
        logger.debug("execute2");
    }
}


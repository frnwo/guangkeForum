package com.guangke.forum;

import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@SpringBootTest
class ForumApplicationTests {
    @Autowired
    UserMapper userMapper;
    @Test
    void contextLoads() {

    }

}

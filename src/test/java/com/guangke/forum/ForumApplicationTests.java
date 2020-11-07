package com.guangke.forum;

import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ForumApplicationTests {
    @Autowired
    UserMapper userMapper;
    @Test
    void contextLoads() {
        User user = userMapper.selectById(101);
        System.out.println(user);
        user = userMapper.selectByUsername("liubei");
        System.out.println(user);
    }

}

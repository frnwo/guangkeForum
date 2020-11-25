package com.guangke.forum;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.mapper.LoginTicketMapper;
import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.pojo.LoginTicket;
import com.guangke.forum.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;


@SpringBootTest
public class mapperTest {
    @Autowired
    UserMapper userMapper;
    @Autowired
    DiscussPostMapper discussPostMapper;
    @Autowired
    LoginTicketMapper loginTicketMapper;
    @Test
    public void testSelect(){
        User user = new User();
        user.setUsername("zjh");
        user.setPassword("123abc");
        user.setCreateTime(new Date());
        user.setEmail("110@qq.com");
        user.setSalt("111");
        int c = userMapper.insertUser(user);
        System.out.println(c);
        System.out.println(user.getId());
    }
    @Test
    public void testUpdates(){
        userMapper.updateHeader(151,"http://img/zjh.png");
        userMapper.updatePassword(151,"newPassword");
        userMapper.updateStatus(151,1);
    }
    @Test
    public void testSelectDiscussPost(){
        List<DiscussPost> list = discussPostMapper.selectDiscussPost(149,0,10,0);
          System.out.println(list);
        int c = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(c);

    }
    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(1001);
        loginTicket.setStatus(0);
        loginTicket.setTicket("a");
        loginTicket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));
        loginTicketMapper.insertLoginTicket(loginTicket);
    }
    @Test
    public void selectLoginTicket(){
        LoginTicket loginTicket = loginTicketMapper.selectLoginTicket("a");
        System.out.println(loginTicket);

        loginTicketMapper.updateLoginTicket("a",1);
        loginTicket = loginTicketMapper.selectLoginTicket("a");
        System.out.println(loginTicket);
    }
}

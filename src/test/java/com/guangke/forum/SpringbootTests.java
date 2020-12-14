package com.guangke.forum;

import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.service.DiscussPostService;
import org.junit.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class SpringbootTests {
    private DiscussPost data;

    @Autowired
    private DiscussPostService discussPostService;


    @BeforeClass
    public static void beforeClass(){
        System.out.println("beforeClass");
    }

    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }
    @Before
    public void before(){
        System.out.println("before");
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("Title Test");
        data.setScore(2000.99);
        data.setContent("Test Content");
        data.setCreateTime(new Date());
        discussPostService.addDiscussPost(data);
    }
    @After
    public void after(){
        discussPostService.updateStatus(data.getId(),2);
    }

    @Test
    public void test1(){
        System.out.println("test1");
    }
    @Test
    public void test2(){
        System.out.println("test2");
    }
    @Test
    public void findPostByIdTest(){
        System.out.println(data.getId());
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assert.assertNotNull(post);
        Assert.assertEquals("Title Test",data.getTitle());
    }
    @Test
    public void updatePostByIdTest(){
        int rows = discussPostService.updateScore(data.getId(),1000.99);
        DiscussPost post = discussPostService.findDiscussPostById(data.getId());
        Assert.assertEquals(1,rows);
        Assert.assertEquals(1000.99,post.getScore(),2);


    }

}

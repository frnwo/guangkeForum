package com.guangke.forum;

import com.guangke.forum.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class SensitiveFilterTest {
    @Autowired
    SensitiveFilter sensitiveFilter;
    @Test
    public void testSensitiveFilter(){
        String text = "我发个自拍，关你屁事啊？又没嫖娼操你le妈的个傻逼哟";
        System.out.println(sensitiveFilter.filter(text));
    }
}

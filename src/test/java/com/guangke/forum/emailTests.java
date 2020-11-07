package com.guangke.forum;

import com.guangke.forum.util.MailClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@SpringBootTest
public class emailTests {
    @Autowired
    private MailClient mailClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Test
    public void sendMailTest(){
        Context context = new Context();
        context.setVariable("username","郑家豪");
        String htmlStr = templateEngine.process("/mail/demo",context);
        System.out.println(htmlStr);
        mailClient.sendMail("2913114765@qq.com","text",htmlStr);
    }

}

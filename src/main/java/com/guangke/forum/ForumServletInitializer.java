package com.guangke.forum;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
//服务器tomcat从这里进入应用。因为springboot 已经内嵌有一个tomcat，不用这个
public class ForumServletInitializer extends SpringBootServletInitializer {
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ForumApplication.class);
    }
}

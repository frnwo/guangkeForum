package com.guangke.forum.config;
import com.guangke.forum.interceptor.AlphaInterceptor;
import com.guangke.forum.interceptor.LoginRequiredInterceptor;
import com.guangke.forum.interceptor.LoginTicketInterceptor;
import com.guangke.forum.interceptor.MessageInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Autowired
    AlphaInterceptor alphaInterceptor;

    @Autowired
    LoginTicketInterceptor loginTicketInterceptor;

    @Autowired
    LoginRequiredInterceptor requiredInterceptor;

    @Autowired
    MessageInterceptor messageInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(alphaInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg")
                .addPathPatterns("/register","/login");
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        //使用security进行登录权限控制
//        registry.addInterceptor(requiredInterceptor)
//                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css","/**/*.js","/**/*.png","/**/*.jpg","/**/*.jpeg");
    }
}

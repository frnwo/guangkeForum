package com.guangke.forum.interceptor;

import com.guangke.forum.mapper.LoginTicketMapper;
import com.guangke.forum.pojo.LoginTicket;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.CookieUtils;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

/**
 * 对于每个请求都会验证是否登录，来决定是否给出用户信息
 */
@Component
public class LoginTicketInterceptor implements HandlerInterceptor {

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    /**
     *在进入controller方法之前先查看浏览器发送过来的cookie有没有登录凭证ticket
     * 如果有的话而且凭证没过期没失效就给线程添加user对象
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Cookie cookie = CookieUtils.getCookie(request,"ticket");
        if(cookie!=null){
            LoginTicket loginTicket = userService.findLoginTicket(cookie.getValue());
            if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                hostHolder.set(user);
            }
        }
        return true;
    }
    /**
     * 在解析视图前,把user放进model
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.get();
        if(user!=null && modelAndView !=null){
            modelAndView.addObject("loginUser",hostHolder.get());
        }
    }

    /**
     * 处理请求的线程使用完之后销毁内部的threadLocals的user，否则线程池重复使用该
     * 线程时user会继续存在
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }
}

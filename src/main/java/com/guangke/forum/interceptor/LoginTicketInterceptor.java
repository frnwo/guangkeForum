package com.guangke.forum.interceptor;

import com.guangke.forum.mapper.LoginTicketMapper;
import com.guangke.forum.pojo.LoginTicket;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.CookieUtils;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
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
        //从cookie中查询ticket
        Cookie cookie = CookieUtils.getCookie(request,"ticket");

        if(cookie!=null){
            //从redis中查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(cookie.getValue());
            //检测凭证是否有效
            if(loginTicket!=null && loginTicket.getStatus()==0 && loginTicket.getExpired().after(new Date())){
                //查询用户
                User user = userService.findUserById(loginTicket.getUserId());
                //在请求中持有用户
                hostHolder.set(user);

                /**因为跳过了security的认证方案，采用了自定义的认证，因此securityContext没有认证结果
                *   而security的权限控制需要认证的结果，需要自己手动添加
                 **/
                Authentication authentication = new UsernamePasswordAuthenticationToken(user,user.getPassword(),userService.getAuthorities(user.getId()));
                SecurityContextHolder.setContext(new SecurityContextImpl(authentication));

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
        //请求结束时，也要清除掉认证信息
        SecurityContextHolder.clearContext();

    }
}

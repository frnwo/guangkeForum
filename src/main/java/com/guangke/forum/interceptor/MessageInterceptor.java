package com.guangke.forum.interceptor;

import com.guangke.forum.pojo.User;
import com.guangke.forum.service.MessageService;
import com.guangke.forum.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class MessageInterceptor implements HandlerInterceptor {

    @Autowired
    HostHolder hostHolder;

    @Autowired
    MessageService messageService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        User user = hostHolder.get();

        if(user!=null && modelAndView != null){

            int letterUnreadCount = messageService.getUnreadMessagesCount(user.getId(),null);
            int noticeUnreadCount = messageService.getNoticeUnreadCount(user.getId(),null);

            modelAndView.addObject("allUnreadCount",letterUnreadCount+noticeUnreadCount);

        }
    }
}

package com.guangke.forum.controller.advice;

import com.guangke.forum.util.ForumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {
    private static Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);

    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        logger.error("服务器发生错误",e.getMessage());
        for(StackTraceElement element : e.getStackTrace()){
            logger.error(element.toString());
        }
        String xRequestedWith = request.getHeader("x-requested-with");
        //异步请求产生异常时，返回json,不用跳转到错误页面
        if("XMLHttpRequest".equals(xRequestedWith)){
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            //浏览器的data 将会接收到这个json格式的字符串
            writer.write(ForumUtils.getJSONString(1,"服务器发生错误"));
        }else {
            //其他非异步请求，重定向到500页面
           response.sendRedirect(request.getContextPath()+"/errorPage");
        }
    }
}

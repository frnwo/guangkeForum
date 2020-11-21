package com.guangke.forum.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Aspect
public class ServiceLogAspect {
    private Logger logger = LoggerFactory.getLogger(ServiceLogAspect.class);
    //定义切点
    @Pointcut("execution(* com.guangke.forum.service.*.*(..))")
    public void pointcut(){}

    //定义通知
    @Before("pointcut()")
    public void before(JoinPoint joinPoint){
        //用户[ip],在[时间],访问了[com.xx.方法]
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //当调用service包中的方法的线程不是请求线程而是消费者线程时，attributes为null
        if(attributes == null){
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String ip = request.getRemoteHost();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String method = joinPoint.getSignature().getDeclaringTypeName()+"."+joinPoint.getSignature().getName();
        logger.info(String.format("用户[%s],在[%s]，访问了[%s].",ip,time,method));
    }
}

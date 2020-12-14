package com.guangke.forum.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

//@Component
//@Aspect
public class AlphaAspect {
    /**
     * 定义切点，使用表达式表明哪些bean的哪些方法需要weaving逻辑处理代码
     */
    //第一个*返回任意类型 第二个* 这个包的任意类  ；第三个* 任意方法

    @Pointcut("execution(* com.guangke.forum.service.*.*(..))")
    public void pointcut(){
        //空
    }

    @Before("pointcut()")
    public void before(){
        System.out.println("before");
    }

    @Around("pointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("Around before");
        //目标方法返回的结果
        Object obj = joinPoint.proceed();
        System.out.println("Around After");
        return obj;
    }

    @After("pointcut()")
    public void after(){
        System.out.println("after");
    }

    @AfterThrowing("pointcut()")
    public void afterThrowing(){
        System.out.println("afterThrowing");
    }

    @AfterReturning("pointcut()")
    public void afterReturning(){
        System.out.println("afterReturning");
    }
}


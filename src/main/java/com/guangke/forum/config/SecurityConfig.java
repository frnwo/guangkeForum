package com.guangke.forum.config;

import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter  implements ForumConstants {
    @Override
    public void configure(WebSecurity web) throws Exception {
        //静态资源不拦截
        web.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers(
                "/user/profile/detail/**",
                "/user/setting",
                "/user/upload",
                "/follow",
                "/unfollow",
                "/letter/**",
                "/like",
                "/notice/**")
                .hasAnyAuthority(
                        AUTHORITY_USER,AUTHORITY_ADMIN,AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/top","/discuss/wonderful")
                .hasAnyAuthority(AUTHORITY_MODERATOR)
                .antMatchers(
                        "/discuss/delete","/data/**")
                .hasAnyAuthority(AUTHORITY_ADMIN)
                .anyRequest().permitAll();
                //如果禁用了csrf，form表单不会生成token，当有权限时异步请求不会被认为是没有权限，正常访问
                //如果开启了csrf,form表单自动生成token,异步请求必须添加相应的header才行
//                .and().csrf().disable();

        //权限不够处理
        http.exceptionHandling()
                //没有登录时的处理
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
                        //请求方式
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //异步
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(ForumUtils.getJSONString(403,"您还没有登录哦！"));
                        }else{
                            //不是异步时跳到登录页面
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
                //登录后权限还是不够时的处理
                .accessDeniedHandler(new AccessDeniedHandler() {
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
                        //请求方式
                        String xRequestedWith = request.getHeader("x-requested-with");
                        //异步
                        if("XMLHttpRequest".equals(xRequestedWith)){
                            response.setContentType("application/plain;charset=utf-8");
                            PrintWriter writer = response.getWriter();
                            writer.write(ForumUtils.getJSONString(403,"您没有访问该功能的权限"));
                        }else{
                            //不是异步时跳到的登录页面
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });

        /**  security 底层默认会拦截/logout退出请求，使用一个假的"/securitylogout"覆盖
         *  才能执行自定义的退出处理
         */
        http.logout().logoutUrl("/securitylogout");


    }
}

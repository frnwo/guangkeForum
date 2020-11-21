package com.guangke.forum.controller;

import com.guangke.forum.util.ForumUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/alpha")
public class AlphaController {
    @GetMapping("/cookie/set")
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        Cookie cookie = new Cookie("code", ForumUtils.generateUUID());
        //cookie的有效路径
        cookie.setPath("/forum/alpha");
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "set Cookie";
    }
    @GetMapping("/cookie/setNum")
    @ResponseBody
    public String setNum(HttpServletResponse response){
        Cookie cookie = new Cookie("num","3");
        //cookie的有效路径
        cookie.setPath("/forum/alpha");
        cookie.setMaxAge(60*10);
        response.addCookie(cookie);
        return "set num 3";
    }
    @GetMapping("/cookie/get")
    @ResponseBody
    public String getCookie(@CookieValue("code") String code,@CookieValue("num") String num){
        return "get Cookie: code-"+code+" ,num-"+num;
    }
    @GetMapping("/session/set")
    @ResponseBody
    public String setSession(HttpSession session){
        System.out.println(session.isNew());
            session.setAttribute("id",1);
            session.setAttribute("name","test");
            return "set session";
    }
    @GetMapping("/session/get")
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }
    @PostMapping("/ajax")
    @ResponseBody
    public String testJSON(String name,int age){
        System.out.println(name+"--"+age);
        return ForumUtils.getJSONString(0,"操作成功");
    }
}

package com.guangke.forum.util;

import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class CookieUtils {
    public static Cookie getCookie(HttpServletRequest request,String name){
        if(request == null || StringUtils.isBlank(name)){
            throw new IllegalArgumentException("参数不能为空");
        }
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(name)){
                    return cookie;
                }
            }
        }
        return null;
    }
}

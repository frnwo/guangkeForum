package com.guangke.forum.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;
import java.util.UUID;

public class ForumUtils {
    //生成随机UUID字符串 用于随机盐
    public static String generateUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
    //加密
    public static  String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}

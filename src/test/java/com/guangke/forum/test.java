package com.guangke.forum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class test {

    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        Long likeCount = 30L;
        Map<String,Object> map  = new HashMap<>();
        map.put("likeCount",likeCount);
        likeCount = 40L;
        System.out.println(map.get("likeCount"));
    }

}


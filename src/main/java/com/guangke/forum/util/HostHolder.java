package com.guangke.forum.util;

import com.guangke.forum.pojo.User;
import org.springframework.stereotype.Component;


@Component
public class HostHolder {
    private ThreadLocal<User> threadLocal = new ThreadLocal<>();
    public void set(User user){
        threadLocal.set(user);
    }
    public User get(){
       return threadLocal.get();
    }
    public void clear(){
        threadLocal.remove();
    }
}

package com.guangke.forum.service;

import com.guangke.forum.mapper.LoginTicketMapper;
import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.LoginTicket;
import com.guangke.forum.pojo.User;
import com.guangke.forum.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;

@Service
public class UserService implements ForumConstants {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${forum.path.domain}")
    private String domain;
//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    RedisTemplate redisTemplate;

    public User findUserById(int id){
//        return userMapper.selectById(id);
        User user = getCache(id);
        if(user == null){
            user = init(id);
        }
        return user;
    }
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();
        if(user==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","用户名不能为空");
           return map;
        }
        if (StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空");
            return map;
        }
        User u = userMapper.selectByUsername(user.getUsername());
        if(u!=null){
            map.put("usernameMsg","用户已存在");
            return map;
        }
        u = userMapper.selectByEmail(user.getEmail());
        if(u!=null){
            map.put("emailMsg","邮箱已被注册");
            return map;
        }

        user.setCreateTime(new Date());
        //普通用户
        user.setType(0);
        //未激活
        user.setStatus(0);
        user.setSalt(ForumUtils.generateUUID().substring(0,5));
        user.setPassword(ForumUtils.md5(user.getPassword()+user.getSalt()));
        user.setActivationCode(ForumUtils.generateUUID());
        String headerUrl = String.format("http://images.nowcoder.com/head/%dt.png",new Random().nextInt(1000));
        user.setHeaderUrl(headerUrl);
        userMapper.insertUser(user);

        return map;
    }
    //http://localhost:8080/forum/activation/userId/code
    //异步发送邮件
    @Async
    public void sendMail(User user){
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        String url = domain+contextPath+"/activation/"+user.getId()+"/"+user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);
    }
    public ActivationStatus activate(int userId, String activationCode){
        User u = userMapper.selectById(userId);
        if(u.getStatus()==1){
            return ActivationStatus.ACTIVATION_REPEAT;
        }else if(u.getActivationCode().equals(activationCode)){
            userMapper.updateStatus(userId,1);
            clearCache(userId);
            return ActivationStatus.ACTIVATION_SUCCESS;
        }else {
            //可能是防止激活没有这个id的用户或者无效激活码
            return ActivationStatus.ACTIVATION_FAILURE;
        }
    }
    public Map<String,Object> login(String username,String password,int ticketSeconds){
        Map<String,Object> map = new HashMap<>();
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","用户名不能为空");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空");
            return map;
        }
        User user = userMapper.selectByUsername(username);
        if(user==null){
            map.put("usernameMsg","用户不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","该用户未激活");
            return map;
        }
        password = ForumUtils.md5(password+user.getSalt());
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg","密码错误");
            return map;
        }
        //到了这一步，说明用户名和密码正确，然后生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setTicket(ForumUtils.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+ ticketSeconds * 1000));
        loginTicket.setUserId(user.getId());
//        loginTicketMapper.insertLoginTicket(loginTicket);
        String ticketKey = RedisKeyUtils.getLoginTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(ticketKey,loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }
    public void logOut(String ticket){
//        loginTicketMapper.updateLoginTicket(ticket,1);
        String ticketKey = RedisKeyUtils.getLoginTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
        //1 无效
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(ticketKey,loginTicket);

    }
    public LoginTicket findLoginTicket(String ticket){
        String ticketKey = RedisKeyUtils.getLoginTicketKey(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(ticketKey);
    }
    public int updateHeaderUrl(int userId,String headerUrl){
        int rows = userMapper.updateHeader(userId,headerUrl);
        clearCache(userId);
        return rows;
    }
    public int  updatePassword(int userId,String newPassword){
        int rows = userMapper.updatePassword(userId,newPassword);
        clearCache(userId);
        return rows;
    }
    public User findUserByName(String username){
        return userMapper.selectByUsername(username);
    }
    //1.优先从缓存中取user信息
    private User getCache(int userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        return (User)redisTemplate.opsForValue().get(userKey);
    }
    //2.缓存没有时再从数据库查，并将初始化到redis
    private User init(int userId){
        User user = userMapper.selectById(userId);
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.opsForValue().set(userKey,user);
        return user;
    }
    //3.当用户信息修改时，清空缓存
    private void clearCache(int userId){
        String userKey = RedisKeyUtils.getUserKey(userId);
        redisTemplate.delete(userKey);
    }
    //获取用户权限
    public List<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();

        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;

    }
}

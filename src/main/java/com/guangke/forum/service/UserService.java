package com.guangke.forum.service;

import com.guangke.forum.mapper.LoginTicketMapper;
import com.guangke.forum.mapper.UserMapper;
import com.guangke.forum.pojo.LoginTicket;
import com.guangke.forum.pojo.User;
import com.guangke.forum.util.ActivationStatus;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.MailClient;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {
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
    @Autowired
    private LoginTicketMapper loginTicketMapper;

    public User findUserById(int id){
        return userMapper.selectById(id);
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
        user.setHeaderUrl("https://source.unsplash.com/random");
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
        loginTicketMapper.insertLoginTicket(loginTicket);
        map.put("ticket",loginTicket.getTicket());
        return map;
    }
    public void logOut(String ticket){
        loginTicketMapper.updateLoginTicket(ticket,1);
    }
}

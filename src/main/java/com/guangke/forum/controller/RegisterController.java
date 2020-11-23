package com.guangke.forum.controller;

import com.google.code.kaptcha.Producer;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ActivationStatus;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.RedisKeyUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
public class RegisterController implements ForumConstants {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private Producer kaptchaProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @Value("${server.servlet.context-path}")
    private String context;

    @GetMapping("/register")
    public String getRegisterPage(){
        return "/site/register";
    }

    @GetMapping("/login")
    public String getLoginPage(){
        return "/site/login";
    }

    @PostMapping("/register")
    public String register(Model model, User user){
        //model里面实例化了一个user,user里面是浏览器提交的表单
        Map<String,Object> map = userService.register(user);
        if(map.isEmpty()){
            //发送激活邮件
            //sendMail()方法是Async,必须在Controller类的请求方法中使用
            userService.sendMail(user);
            model.addAttribute("msg","我们向您发了一封激活邮件，请尽快激活");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }
    }
    @GetMapping("/activation/{userId}/{activationCode}")
    public String activate(Model model,@PathVariable("userId") int userId, @PathVariable("activationCode")String activationCode){
        ActivationStatus status = userService.activate(userId,activationCode);
        if(status == ActivationStatus.ACTIVATION_SUCCESS){
            model.addAttribute("msg","您的账号已激活成功");
            model.addAttribute("target","/login");
        }else if(status == ActivationStatus.ACTIVATION_REPEAT){
            model.addAttribute("msg","该用户已经激活过了");
            model.addAttribute("target","/index");
        }else {
            model.addAttribute("msg","无效的激活码");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }
    @GetMapping("/kaptcha")
    public void getKaptcha(HttpServletResponse response/*, HttpSession session*/){
        String text = kaptchaProducer.createText();
        /**
         * 生成随机值来标识验证码值，
         * 将该随机值和验证码组成key-value存到redis
         * 将该随机值放入cookie发给浏览器。登录请求中的cookie带有该随机值,通过该key找到的value不为null说明验证码正确
         *
         * */
        //随机值
        String owner = ForumUtils.generateUUID();
        //验证码 key
        String kaptchaKey = RedisKeyUtils.getKaptchaKey(owner);
        redisTemplate.opsForValue().set(kaptchaKey,text,60, TimeUnit.SECONDS);
        //带有该key的cookie发给浏览器
        Cookie cookie = new Cookie("kaptchaKey",owner);
        cookie.setMaxAge(60);
        cookie.setPath(context);
        response.addCookie(cookie);
//        session.setAttribute("kaptcha",text);
        BufferedImage image = kaptchaProducer.createImage(text);
        response.setContentType("image/png");
        try {
            ImageIO.write(image,"png",response.getOutputStream());
        } catch (IOException e) {
            logger.error("响应验证码失败: "+e.getMessage());
        }
    }
    @PostMapping("/login")
    public String login(String username,String password,String code,/*HttpSession session,*/HttpServletResponse response,
                        Model model,boolean rememberme,@CookieValue("kaptchaKey") String owner){

//        String kaptcha = (String)session.getAttribute("kaptcha");
        //检查验证码
        String kaptcha = null;
        //如果请求cookie中有kaptchaKey,则从redis获取验证码
        if(StringUtils.isNotBlank(owner)){
            String kaptchaKey = RedisKeyUtils.getKaptchaKey(owner);
            kaptcha = (String) redisTemplate.opsForValue().get(kaptchaKey);
        }

        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("code","验证码不正确");
            return "/site/login";
        }
        int ticketSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,ticketSeconds);
        //如果map中没有ticket键，则登录出了问题
        if(!map.containsKey("ticket")){
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }else {
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setMaxAge(ticketSeconds);
            cookie.setPath(context);
            response.addCookie(cookie);
            //重定向
            return "redirect:/index";
        }
    }
    @GetMapping("/logout")
    public String logOut(@CookieValue("ticket") String ticket){
        userService.logOut(ticket);
        SecurityContextHolder.clearContext();
        //login有两个 重定向默认跳转方式为get
        return "redirect:/login";
    }

}

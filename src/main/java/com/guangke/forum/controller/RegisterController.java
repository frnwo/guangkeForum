package com.guangke.forum.controller;

import com.google.code.kaptcha.Producer;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ActivationStatus;
import com.guangke.forum.util.ForumConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Controller
public class RegisterController implements ForumConstants {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);
    @Autowired
    UserService userService;

    @Autowired
    Producer kaptchaProducer;

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
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        String text = kaptchaProducer.createText();
        session.setAttribute("kaptcha",text);
        BufferedImage image = kaptchaProducer.createImage(text);
        response.setContentType("image/png");
        try {
            ImageIO.write(image,"png",response.getOutputStream());
        } catch (IOException e) {
            logger.error("响应验证码失败: "+e.getMessage());
        }
    }
    @PostMapping("/login")
    public String login(String username,String password,String code,HttpSession session,HttpServletResponse response,Model model,boolean rememberme){
        //检查验证码
        String kaptcha = (String)session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !code.equalsIgnoreCase(kaptcha)){
            model.addAttribute("code","验证码不正确");
            return "/site/login";
        }
        int ticketSeconds = rememberme ? REMEMBER_EXPIRED_SECONDS : DEFAULT_EXPIRED_SECONDS;
        Map<String,Object> map = userService.login(username,password,ticketSeconds);
        if(!map.containsKey("ticket")){
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            return "/site/login";
        }else {
            Cookie cookie = new Cookie("ticket", (String) map.get("ticket"));
            cookie.setMaxAge(ticketSeconds);
            cookie.setPath(context);
            logger.info(context);
            response.addCookie(cookie);
            //重定向
            return "redirect:/index";
        }
    }
    @GetMapping("/logout")
    public String logOut(@CookieValue("ticket") String ticket){
        userService.logOut(ticket);
        //login有两个 重定向默认跳转方式为get
        return "redirect:/login";
    }
}

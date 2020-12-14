package com.guangke.forum.controller;

import com.guangke.forum.annotation.LoginRequired;
import com.guangke.forum.pojo.User;
import com.guangke.forum.service.FollowService;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.service.UserService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements ForumConstants {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String context;

    @Value("${forum.path.uploadImage}")
    private String uploadPath;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;

    @LoginRequired
    @GetMapping("/setting")
    public String getSetting(Model model){
        //上传文件名称
        String fileName = ForumUtils.generateUUID();
        //设置相应信息
        StringMap policy = new StringMap();
        policy.put("returnBody",ForumUtils.getJSONString(0));
        //设置上传凭证
        Auth auth = Auth.create(accessKey,secretKey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return "/site/setting";
    }

    //更新headerUrl
    @PostMapping(path = "/header/url")
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)){
           ForumUtils.getJSONString(1,"文件名不能为空");
        }
        String headerUrl = headerBucketUrl+"/"+fileName;
        userService.updateHeaderUrl(hostHolder.get().getId(),headerUrl);
        return ForumUtils.getJSONString(0);
    }

    //废弃 改为在js异步提交到七牛云
    @LoginRequired
    @PostMapping("/upload")
    public String upload(MultipartFile image, Model model){
        //图片为空时，中断
        if(image == null){
            model.addAttribute("error","图片文件不能为空");
            return "/site/setting";
        }
        //给图片生成随机名
        String fileName = image.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf('.'));
        //后缀为空时，中断
        if(!(suffix.equals(".jpg") || suffix.equals(".png")||suffix.equals(".jpeg"))){
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }
        fileName = ForumUtils.generateUUID()+suffix;

        //将上传图片复制到本地路径
        File file = new File(uploadPath+"/"+fileName);
        try {
            image.transferTo(file);
        } catch (IOException e) {
            logger.error("图片上传失败");
            throw new RuntimeException("图片上传失败，服务器发生错误: "+e.getMessage());
        }

        /**
            修改用户信息中的图片路径headerUrl : http://localhost:8080/forum/user/profile/xxx.jpg
         */
        String headerUrl = domain+context+"/user/profile/"+fileName;
        userService.updateHeaderUrl(hostHolder.get().getId(),headerUrl);
        return "redirect:/index";
    }


    //废弃  改为从七牛云获取图片
    /**
     * 响应用户头像图片
     */
    @GetMapping("/profile/{fileName}")
    public void getProfile(@PathVariable("fileName") String fileName, HttpServletResponse response){
        String dest = uploadPath+"/"+fileName;
        String suffix = fileName.substring(fileName.lastIndexOf('.')+1);
        response.setContentType("image/"+suffix);
        try(
                //获取响应流
                OutputStream os = response.getOutputStream();
                //读取本地文件
                FileInputStream fis = new FileInputStream(dest)
        ){
                byte[] buffer = new byte[1024];
                int b = -1;
                while((b=fis.read(buffer))!=-1){
                    os.write(buffer,0,b);
                }
        }catch (Exception e){
            logger.error("读取/响应文件失败:"+e.getMessage());
        }
    }
    /**
     * 修改密码
     */
    @PostMapping("/updatePassword")
    public String updatePassword(String oldPassword,String newPassword,Model model){
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("oldPasswordMsg","原密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("newPasswordMsg","新密码不能为空");
            return "/site/setting";
        }
        /**
         *验证旧密码与当前用户的密码
         */
        //取出真正原密码(如果有登录凭证，则在Hostholder可以取出用户信息)
        User user = hostHolder.get();
        String password = hostHolder.get().getPassword();
        String salt = hostHolder.get().getSalt();
        //对提交的旧密码加密
        oldPassword = ForumUtils.md5(oldPassword+salt);
        if(!oldPassword.equals(password)){
            model.addAttribute("oldPasswordMsg","旧密码不正确");
            return "/site/setting";
        }
        //进行到这里时说明可以对数据库的密码修改
        newPassword = ForumUtils.md5(newPassword+salt);
        userService.updatePassword(user.getId(),newPassword);
        /**
         * 不能是forward,必须是重定向，否则服务器是直接发送index.html给浏览器，这样的话就不是浏览器请求
         * controller的index路径，导致index模板的分页信息和帖子信息空缺
         */
        return "redirect:/index";
    }
    //个人详情页面
    @GetMapping(path = "/profile/detail/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("当前用户不存在！");
        }
        model.addAttribute("user",user);
        //点赞数
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数
        long followeeCount =  followService.getFolloweeCount(userId,ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //粉丝数
        long followerCount = followService.getFollowerCount(ENTITY_TYPE_USER,userId);
        model.addAttribute("followerCount",followerCount);

        //当前用户对该实体的关注状态
        boolean hasFollowed = false;
        //如果未登录则默认为false显示为 未关注,登录时查询是否有关注该实体
        if(hostHolder.get() != null){
            hasFollowed = followService.hasFollowed(hostHolder.get().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);
        return "/site/profile";
    }

}

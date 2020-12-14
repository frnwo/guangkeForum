package com.guangke.forum.controller;

import com.guangke.forum.event.EventProducer;
import com.guangke.forum.pojo.Event;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.ForumUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WkController implements ForumConstants {
    private Logger logger = LoggerFactory.getLogger(WkController.class);

    @Autowired
    private EventProducer producer;

    @Value("${forum.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String context;

    @Value("${wk.images.storage}")
    private String wkImageStorage;

    @Value("${qiniu.bucket.share.url}")
    private String shareUrl;

    //将htmlUrl生成长图,返回长图的访问路径
    @GetMapping(path = "/share")
    @ResponseBody
    public String share(String htmlUrl){
        //文件名
        String fileName = ForumUtils.generateUUID();
        //异步生成长图
        Event event = new Event()
                .setData("htmlUrl",htmlUrl)
                .setData("fileName",fileName)
                .setData("suffix","png");

        producer.fireEvent(TOPIC_SHARE,event);

        Map<String,Object> map = new HashMap<>();
//        map.put("shareUrl",domain+context+"/share/images/"+fileName);
        map.put("shareUrl",shareUrl+"/"+fileName);

        return ForumUtils.getJSONString(0,null,map);
    }

    //废弃 改为从七牛云获取
    @GetMapping(path = "/share/images/{fileName}")
    public void getWkImages(@PathVariable("fileName") String fileName, HttpServletResponse response){
        if(StringUtils.isBlank(fileName)){
            throw new IllegalArgumentException("参数不能为空");
        }
        File file = new File(wkImageStorage+"/"+fileName+".png");
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int b = -1;
            while((b = fis.read(buffer))!=-1){
                os.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("获取长图失败 ："+e.getMessage());
        }
    }

}

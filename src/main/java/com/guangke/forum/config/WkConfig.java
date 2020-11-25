package com.guangke.forum.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WkConfig {
    private Logger logger = LoggerFactory.getLogger(WkConfig.class);

    @Value("${wk.images.storage}")
    private String storage;

    //初始化长图的存放目录
    @PostConstruct
    public void init(){
        File file = new File(storage);
        if(!file.exists()){
            file.mkdirs();
            logger.info("创建长图存放目录: "+storage);
        }
    }
}

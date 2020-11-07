package com.guangke.forum;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class loggerTests {
    private static final Logger logger = LoggerFactory.getLogger(loggerTests.class);
    @Test
    public void test(){
        logger.debug("this is debug2");
        logger.info("this is info2");
        logger.warn("this is warn2");
        logger.error("this is error2");
    }
}

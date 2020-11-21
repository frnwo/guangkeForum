package com.guangke.forum;

import com.guangke.forum.service.AlpahService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TransactionTest {
    @Autowired
    AlpahService alpahService;
    @Test
    public void testInsert(){
       Object obj =  alpahService.addUserAndPost();
        System.out.println(obj);
    }
}

package com.guangke.forum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTests {

    @Autowired
    RedisTemplate<String,Object> redisTemplate;

    @Test
    public void testString(){
        String key = "test:count";
        redisTemplate.opsForValue().set(key,1);
        redisTemplate.opsForValue().get(key);
        redisTemplate.opsForValue().increment(key);
        redisTemplate.opsForValue().decrement(key);
    }

    @Test
    public void testHash(){
        String key = "test:user";
        redisTemplate.opsForHash().put(key,"id",1);
        redisTemplate.opsForHash().put(key,"username","zjh");
        System.out.println(redisTemplate.opsForHash().get(key,"id"));
        System.out.println(redisTemplate.opsForHash().get(key,"username"));

    }

    @Test
    public void testList(){
        String key = "test:ids";
        redisTemplate.opsForList().leftPush(key,"a");
        redisTemplate.opsForList().leftPush(key,"b");
        redisTemplate.opsForList().leftPush(key,"c");
        System.out.println( redisTemplate.opsForList().size(key));//3
        System.out.println( redisTemplate.opsForList().index(key,0));//c
        System.out.println( redisTemplate.opsForList().range(key,1,2));//b,a
        redisTemplate.opsForList().leftPop(key);//c
        redisTemplate.opsForList().leftPop(key);//b
        redisTemplate.opsForList().leftPop(key);//a

    }
    @Test
    public void testSet(){
        String key = "test:roles";
        redisTemplate.opsForSet().add(key,"刘备","关羽","张飞");
        System.out.println(redisTemplate.opsForSet().size(key));//3
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));
    }
    @Test
    public void testZset(){
        String key = "test:heroes";
        redisTemplate.opsForZSet().add(key,"孙悟空",90);
        redisTemplate.opsForZSet().add(key,"唐僧",100);
        redisTemplate.opsForZSet().add(key,"沙悟净",70);
        System.out.println(redisTemplate.opsForZSet().zCard(key));//3
        System.out.println(redisTemplate.opsForZSet().score(key,"沙悟净"));//70
        System.out.println(redisTemplate.opsForZSet().reverseRank(key,"孙悟空"));//1
        System.out.println(redisTemplate.opsForZSet().reverseRange(key,0,-1));
    }
    @Test
    public void testKey(){
        redisTemplate.delete("test:user");
        System.out.println(redisTemplate.hasKey("test:user"));
        redisTemplate.expire("test:roles",15, TimeUnit.SECONDS);
    }
    @Test
    public void testBound(){
        String key="test:count";
        BoundValueOperations<String,Object> operations = redisTemplate.boundValueOps(key);
        for(int i=0;i<5;i++){
            operations.increment();
        }
        System.out.println(operations.get());
    }

    @Test
    public void testTransactional(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public  Object execute(RedisOperations redisOperations) throws DataAccessException {
                String key = "test:tx";
                redisOperations.multi();
                redisOperations.opsForSet().add(key,"a");
                redisOperations.opsForSet().add(key,"b");
                redisOperations.opsForSet().add(key,"c");
//                System.out.println(redisOperations.opsForSet().members(key));
                return redisOperations.exec();
            }
        });
        System.out.println(obj);
    }

}


package com.guangke.forum;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;

import java.util.ArrayList;
import java.util.List;
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
    @Test
    public void hyperLoglogTest(){
        String redisKey = "key:hll:01";
        for (int i = 0; i < 100000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }
        for (int i = 0; i < 100000; i++) {
            int r = (int) (Math.random()*10000);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey));
    }

    @Test
    public void hyperLogLogUnionTest(){
        String redisKey2 = "test:hll:02";
        for (int i = 0; i <10000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 5000; i <15000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 10000; i <20000 ; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }
        String redisKey5 = "test:hll:05";
        redisTemplate.opsForHyperLogLog().union(redisKey5,redisKey2,
                redisKey3,redisKey4);
        System.out.println(redisTemplate.opsForHyperLogLog().size(redisKey5));
    }

    //统计一组数据的布尔值
    @Test
    public void bitmapTest(){
        String redisKey = "test:bitmap:01";
        //记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        //查询
        System.out.println( redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println( redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println( redisTemplate.opsForValue().getBit(redisKey,2));

        //统计
       Object obj = redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                return redisConnection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
    }

    //统计3组数据 or运算
    @Test
    public void bitmapORTest(){
        String redisKey2 = "test:bitmap:02";
        redisTemplate.opsForValue().setBit(redisKey2,0,true);
        redisTemplate.opsForValue().setBit(redisKey2,1,true);

        String redisKey3 = "test:bitmap:03";
        redisTemplate.opsForValue().setBit(redisKey2,1,true);
        redisTemplate.opsForValue().setBit(redisKey2,2,true);

        String redisKeyOr = "test:bitmap:or";

        Object obj = redisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR,redisKeyOr.getBytes(),redisKey2.getBytes(),redisKey3.getBytes());
                return redisConnection.bitCount(redisKeyOr.getBytes());
            }
        });
        System.out.println(obj);

    }
}


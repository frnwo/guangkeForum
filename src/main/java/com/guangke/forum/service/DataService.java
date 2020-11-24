package com.guangke.forum.service;

import com.guangke.forum.util.ForumUtils;
import com.guangke.forum.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class DataService {
    @Autowired
    private RedisTemplate redisTemplate;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    //记录uv 使用HyperLogLog
    public void recordUv(String ip){
        String uvKey = RedisKeyUtils.getUvKey(sdf.format(new Date()));
        redisTemplate.opsForHyperLogLog().add(uvKey,ip);
    }

    //查询日期区间uv
    public long getUv(Date start,Date end){

        if(start == null ||end == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //先整理每天uv的key
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        List<String> keyList= new ArrayList<>();
        while(!calendar.getTime().after(end)){
            keyList.add(RedisKeyUtils.getUvKey(sdf.format(calendar.getTime())));
            calendar.add(Calendar.DATE,1);
        }
        //再并集运算
        String totalUvKey = RedisKeyUtils.getUvKey(sdf.format(start),sdf.format(end));
        redisTemplate.opsForHyperLogLog().union(totalUvKey,keyList.toArray());
        return redisTemplate.opsForHyperLogLog().size(totalUvKey);
    }

    //记录dau
    public void recordDau(int userId){
        String dauKey = RedisKeyUtils.getDauKey(sdf.format(new Date()));
        redisTemplate.opsForValue().setBit(dauKey,userId,true);
    }

    //查询区间dau
    public long getDau(Date start,Date end) {

        if (start == null || end == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        //先整理每天dau的key
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);
        List<byte[]> keyList = new ArrayList<>();
        while (!calendar.getTime().after(end)) {
            String key = RedisKeyUtils.getDauKey(sdf.format(calendar.getTime()));
            keyList.add(key.getBytes());
            calendar.add(Calendar.DATE, 1);
        }

        return (long) redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                String totalUvKey = RedisKeyUtils.getDauKey(sdf.format(start), sdf.format(end));
                redisConnection.bitOp(RedisStringCommands.BitOperation.OR, totalUvKey.getBytes(),
                        keyList.toArray(new byte[0][0]));
                return redisConnection.bitCount(totalUvKey.getBytes());
            }
        });
    }
}

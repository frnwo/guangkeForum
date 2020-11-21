package com.guangke.forum.service;

import com.guangke.forum.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    RedisTemplate redisTemplate;

    //点赞：redis的set数据类型，如果用户点赞则把该userId 加入该集合，否则取消点赞把该userId从set中remove
    //entityUserId:帖子或评论的作者，点赞是对给作者拥有的赞数量递增或递减
    public void like(int userId,int entityType,int entityId,int entityUserId){
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtils.getEntityLikeKey(entityType,entityId);

                String userLikeKey = RedisKeyUtils.getUserLikeKey(entityUserId);

                boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey,userId);
                //事务
                operations.multi();
                if(isMember){
                    redisTemplate.opsForSet().remove(entityLikeKey,userId);
                    redisTemplate.opsForValue().decrement(userLikeKey);
                }else{
                    redisTemplate.opsForSet().add(entityLikeKey,userId);
                    redisTemplate.opsForValue().increment(userLikeKey);
                }
                return operations.exec();
            }
        });

    }

    //根据实体类型(key=like:entity:entityType:entityId)查询点赞数量
    public long findLikeCount(int entityType,int entityId){
        String likeKey = RedisKeyUtils.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(likeKey);
    }

    //用户的点赞状态 1：已点赞 0:未点赞
    public int findLikeStatus(int userId,int entityType,int entityId){
        String likeKey = RedisKeyUtils.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(likeKey,userId) ?  1 : 0;
    }

    //获得用户的点赞数
    public int  findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtils.getUserLikeKey(userId);
        Integer userLikeCount = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return userLikeCount == null ? 0 : userLikeCount.intValue();
    }
}

package com.guangke.forum.service;

import com.guangke.forum.pojo.User;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.RedisKeyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FollowService implements ForumConstants {
    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserService userService;

    //关注
    public void follow(int userId, int entityType, int entityId) {


        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().add(followeeKey, entityId, System.currentTimeMillis());
                redisTemplate.opsForZSet().add(followerKey, userId, System.currentTimeMillis());
                return operations.exec();
            }
        });
    }

    //取消关注
    public void unfollow(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
                String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);
                operations.multi();
                redisTemplate.opsForZSet().remove(followeeKey, entityId);
                redisTemplate.opsForZSet().remove(followerKey, userId);
                return operations.exec();
            }
        });
    }

    //某个实体(目前指用户)的粉丝数
    public long getFollowerCount(int entityType, int entityId) {
        String followerKey = RedisKeyUtils.getFollowerKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(followerKey);
    }

    //某个用户的关注的实体数量
    public long getFolloweeCount(int userId, int entityType) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(followeeKey);
    }

    //某用户是否已关注某实体
    public boolean hasFollowed(int userId, int entityType, int entityId) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, entityType);
        return redisTemplate.opsForZSet().score(followeeKey, entityId) != null;
    }

    //某用户关注的实体对象集合
    public List<Map<String, Object>> findFollowees(int userId, int offset, int limit) {
        String followeeKey = RedisKeyUtils.getFolloweeKey(userId, ENTITY_TYPE_USER);
        Set<Integer> entityIds = redisTemplate.opsForZSet().reverseRange(followeeKey, offset, offset + limit - 1);
        if (entityIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer entityId : entityIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(entityId);
            map.put("user", user);
            //时间
            Double score = redisTemplate.opsForZSet().score(followeeKey, entityId);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }

    //某实体(用户)的粉丝集合
    public List<Map<String, Object>> findFollowers(int userId, int offset, int limit) {
        String followerKey = RedisKeyUtils.getFollowerKey(ENTITY_TYPE_USER, userId);
        Set<Integer> userIds = redisTemplate.opsForZSet().reverseRange(followerKey, offset, offset + limit - 1);
        if (userIds == null) {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (Integer id : userIds) {
            Map<String, Object> map = new HashMap<>();
            User user = userService.findUserById(id);
            map.put("user", user);
            //时间
            Double score = redisTemplate.opsForZSet().score(followerKey, id);
            map.put("followTime", new Date(score.longValue()));
            list.add(map);
        }
        return list;
    }
}

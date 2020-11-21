package com.guangke.forum.util;

/**
 * redis的key
 */
public class RedisKeyUtils {

    private static final String SPLIT = ":";

    //实体的赞：实体有帖子和评论
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    //用户发的帖子或评论得到的赞： key 的前缀
    private static final String PREFIX_USER_LIKE = "like:user";

    //某用户关注的实体  key的前缀
    private static final String PREFIX_FOLLOWEE = "followee";

    //某实体的粉丝  key的前缀
    private static final String PREFIX_FOLLOWER = "follower";

    //验证码 key的前缀
    private static final String PREFIX_KAPTCHA = "kaptcha";

    //登录凭证 key的前缀
    private static final String PREFIX_TICKET = "ticket";

    //用户 key的前缀
    private static  final String PREFIX_USER = "user";

    //点赞的key: like:entity:entityType:entityId
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }

    //用户得到赞的key: like:user:userId
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    //某个用户关注的实体  key，目前这个项目的实体指的是用户，未来可以根据需要扩展成关注帖子或评论
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    //某个实体的粉丝数 key
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    //验证码key  kaptcha:owner  owner为标识验证码的随机值
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;
    }
    //登录凭证的key ticket:
    public static String getLoginTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }
    //用户 key= user:userId
    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }
}

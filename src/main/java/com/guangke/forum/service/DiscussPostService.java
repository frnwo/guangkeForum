package com.guangke.forum.service;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.util.SensitiveFilter;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DiscussPostService {
    private Logger logger = LoggerFactory.getLogger(DiscussPostService.class);

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    //缓存的页数
    @Value("${caffeine.posts.max-size}")
    private int maxSize;

    @Value("${caffeine.posts.expired-seconds}")
    private int expiredSeconds;

    //热帖缓存
    private LoadingCache<String,List<DiscussPost>> postListCache;

    //帖子数量缓存
    private LoadingCache<Integer,Integer> postRowsCache;

    @PostConstruct
    private void init(){
        System.out.println("调用init()");
        System.out.println();
        //初始化热门帖子列表缓存
        postListCache = Caffeine.newBuilder()
                .expireAfterWrite(expiredSeconds, TimeUnit.SECONDS)
                .maximumSize(maxSize)
                .build(new CacheLoader<String, List<DiscussPost>>() {
                    @Nullable
                    @Override
                    //当缓存没有key时，从数据库里查询
                    public List<DiscussPost> load(@NonNull String key) throws Exception {
                        if(key == null || key.length() == 0){
                            throw new RuntimeException("参数错误");
                        }
                        String[] params = key.split(":");

                        if(params == null || params.length != 2){
                            throw new RuntimeException("参数错误");
                        }

                        int offset = Integer.valueOf(params[0]);
                        int limit = Integer.valueOf(params[1]);

                        logger.info("从数据库查询posts");
                        return discussPostMapper.selectDiscussPost(0,offset,limit,1);
                    }
                });

        postRowsCache = Caffeine.newBuilder()
                .expireAfterWrite(expiredSeconds,TimeUnit.SECONDS)
                .maximumSize(1)
                .build(new CacheLoader<Integer, Integer>() {
                    @Nullable
                    @Override
                    public Integer load(@NonNull Integer key) throws Exception {
                        logger.info("从数据库查询某用户的发帖数量");
                        return discussPostMapper.selectDiscussPostRows(key);
                    }
                });
    }

    //当userId为0，orderMode为1是查询首页热帖，从缓存中获取
    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit,int orderMode){
        if(userId == 0 && orderMode == 1){
            //每页缓存数据的key为offset:limit
            return postListCache.get(offset+":"+limit);
        }
        logger.info("从数据库查询posts");
        return  discussPostMapper.selectDiscussPost(userId,offset,limit,orderMode);
    }

    //当userId为0 是查询帖子数量，这个帖子数量再怎么变化也只是影响分页的数量，所以不是很重要，没必要总是查询
    public int findDiscussPostRows(int userId){
        if(userId == 0){
           return postRowsCache.get(userId);
        }
        logger.info("从数据库查询某用户的发帖数量");
        return discussPostMapper.selectDiscussPostRows(userId);
    }
    public int addDiscussPost(DiscussPost post){
        if(post == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        //转义
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        //过滤敏感词
        post.setContent(sensitiveFilter.filter(post.getContent()));
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        return discussPostMapper.insertDiscussPost(post);
    }
    public DiscussPost findDiscussPostById(int id){
       return discussPostMapper.selectDiscussPostById(id);
    }
    public int updateCommentCount(int postId,int count){
        return discussPostMapper.updateCommentCount(postId,count);
    }

    //0:普通 1：置顶
    public int updateType(int postId,int type){
       return discussPostMapper.updateType(postId,type);
    }

    //0:正常 1：加精 2：删除
    public int updateStatus(int postId,int status){
        return discussPostMapper.updateStatus(postId,status);
    }

    public int updateScore(int postId,double score){
        return discussPostMapper.updateScore(postId,score);
    }

}

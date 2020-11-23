package com.guangke.forum.service;

import com.guangke.forum.mapper.DiscussPostMapper;
import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    DiscussPostMapper discussPostMapper;

    @Autowired
    SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return  discussPostMapper.selectDiscussPost(userId,offset,limit);
    }

    public int findDiscussPostRows(int userId){
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


}

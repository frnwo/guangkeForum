package com.guangke.forum.quartz;

import com.guangke.forum.pojo.DiscussPost;
import com.guangke.forum.service.CommentService;
import com.guangke.forum.service.DiscussPostService;
import com.guangke.forum.service.LikeService;
import com.guangke.forum.service.SearchService;
import com.guangke.forum.util.ForumConstants;
import com.guangke.forum.util.RedisKeyUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, ForumConstants {

    private static Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    //论坛网站纪元
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2020-11-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException();
        }
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private SearchService searchService;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtils.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0){
            logger.info("[任务取消] 没有帖子要刷新！");
            return;
        }
        logger.info("[任务开始] 开始刷新帖子分数");
        while(operations.size() > 0){
            this.refresh((Integer) operations.pop());
        }
        logger.info("[任务结束] 所有帖子分数刷新完毕");
    }

    public void refresh(int postId){
        DiscussPost post = discussPostService.findDiscussPostById(postId);
        /**如果帖子点赞了或评论了等等一些改变分数的行为，redis里面已经有该帖子，如果
        *管理员又把该帖子删除了，那就没有算分数的必要了
         */
        if(post == null){
            logger.info("该帖子已经被删除 id="+postId);
            return;
        }

        /**
         * 以下变量是算分公式的考虑元素
         */
        //是否精华
        boolean wonderful = post.getStatus() == 1;
        //评论数量
        int commentCount = post.getCommentCount();
        //点赞数量
        long likeCount = likeService.findLikeCount(ENTITY_TYPE_DISCUSSPOST,postId);
        //计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        //分数 = 权重 + 距离天数
        double score = Math.log10(Math.max(w,1)) +
                (post.getCreateTime().getTime() - epoch.getTime())/(1000 * 3600 * 24);
         //更新帖子分数
        discussPostService.updateScore(postId,score);
        //更新es服务器
        post.setScore(score);
        searchService.save(post);

    }
}

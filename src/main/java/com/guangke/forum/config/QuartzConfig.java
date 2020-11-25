package com.guangke.forum.config;

import com.guangke.forum.quartz.AlphaJob;
import com.guangke.forum.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

//配置--> 数据库 -->调用
@Configuration
public class QuartzConfig {

    //配置jobDetail
//    @Bean
    public JobDetailFactoryBean alphaJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(AlphaJob.class);
        factoryBean.setDurability(true);
        factoryBean.setGroup("alphaJobGroup");
        factoryBean.setName("alphaJob");
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }

    //配置Trigger
//    @Bean
    public SimpleTriggerFactoryBean alphaTrigger(JobDetail alphaJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(alphaJobDetail);
        factoryBean.setRepeatInterval(3000);
        factoryBean.setGroup("alphaTriggerGroup");
        factoryBean.setName("alphaTrigger");
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }

    @Bean
    public JobDetailFactoryBean postScoreRefreshJobDetail(){
        JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
        factoryBean.setJobClass(PostScoreRefreshJob.class);
        factoryBean.setDurability(true);
        factoryBean.setGroup("forumJobGroup");
        factoryBean.setName("postScoreRefreshJob");
        factoryBean.setRequestsRecovery(true);

        return factoryBean;
    }

    @Bean
    public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail){
        SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
        factoryBean.setJobDetail(postScoreRefreshJobDetail);
        factoryBean.setRepeatInterval(1000 * 60 * 5);
        factoryBean.setGroup("forumTriggerGroup");
        factoryBean.setName("postScoreRefreshTrigger");
        factoryBean.setJobDataMap(new JobDataMap());

        return factoryBean;
    }
}

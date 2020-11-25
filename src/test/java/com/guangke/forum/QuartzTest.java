package com.guangke.forum;

import org.junit.jupiter.api.Test;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class QuartzTest {
    @Autowired
    private Scheduler scheduler;
    @Test
    public void deleteTest() throws SchedulerException {
        scheduler.deleteJob(new JobKey("alphaJob","alphaJobGroup"));
    }
}

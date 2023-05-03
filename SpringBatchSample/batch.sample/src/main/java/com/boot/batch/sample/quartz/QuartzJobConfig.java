package com.boot.batch.sample.quartz;
import lombok.RequiredArgsConstructor;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;

@Configuration
@RequiredArgsConstructor
public class QuartzJobConfig {
    @Value("${schedule.quartz.cron-expression.quartzChunk}")
    private String quartzChunkCron;
    @Value("${schedule.quartz.cron-expression.quartzTasklet}")
    private String quartzTaskletCron;
    private final Scheduler scheduler;

    @PostConstruct
    public void run(){
        JobDetail quartzChunkDetail = runJobDetail(QuartzChunk.class, new HashMap<>());
        JobDetail quartzTaskletDetail = runJobDetail(QuartzTasklet.class, new HashMap<>());

        try {
            // 크론형식 지정 후 스케줄 시작
            scheduler.scheduleJob(quartzChunkDetail, runJobTrigger(quartzChunkCron));
            scheduler.scheduleJob(quartzTaskletDetail, runJobTrigger(quartzTaskletCron));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

    }

    public Trigger runJobTrigger(String scheduleExp){
        // 크론 스케줄 사용
        return TriggerBuilder.newTrigger()
                .withSchedule(CronScheduleBuilder.cronSchedule(scheduleExp)).build();
    }

    public JobDetail runJobDetail(Class job, Map params){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.putAll(params);
        // 스케줄 생성
        return newJob(job).usingJobData(jobDataMap).build();
    }
}
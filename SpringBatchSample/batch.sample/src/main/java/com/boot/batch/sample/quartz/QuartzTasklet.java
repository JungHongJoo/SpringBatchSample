package com.boot.batch.sample.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzTasklet extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job taskletJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("10초마다 Job 실행");

        try {
            jobLauncher.run(taskletJob, new JobParametersBuilder().addString("datetime", LocalDateTime.now().toString()).toJobParameters());
        } catch (Exception e) {
            log.error("Job 실행 중 오류가 발생하였습니다.", e);
        }

    }
}

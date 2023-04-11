package com.boot.batch.sample.quartz;

import lombok.RequiredArgsConstructor;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzMain extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job job;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        log.info("10초마다 Job 실행");

        /*try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("JobID", String.valueOf(System.currentTimeMillis()))
                    .toJobParameters();
            JobExecution jobExecution = jobLauncher.run(job, jobParameters);
            log.info("Batch Job completed with status: {}", jobExecution.getStatus());
        } catch (Exception e) {
            log.error("Batch Job failed: {}", e.getMessage());
        }*/

    }
}

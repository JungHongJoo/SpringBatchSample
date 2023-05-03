package com.boot.batch.sample.config.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
@Slf4j
public class TaskletJobListener implements JobExecutionListener {
    @Override
    public void beforeJob(JobExecution jobExecution){
        log.info("TaskletExampleChunkJobConfiguration start");
    }
    @Override
    public void afterJob(JobExecution jobExecution){
        log.info("TaskletExampleChunkJobConfiguration end");
    }
}

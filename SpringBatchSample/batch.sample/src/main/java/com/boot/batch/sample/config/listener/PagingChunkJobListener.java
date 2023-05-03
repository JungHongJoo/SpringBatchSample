package com.boot.batch.sample.config.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
@Slf4j
public class PagingChunkJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution){
        log.info("PagingExampleChunkJobConfiguration start");
    }
    @Override
    public void afterJob(JobExecution jobExecution){
        log.info("PagingExampleChunkJobConfiguration end");
    }
}
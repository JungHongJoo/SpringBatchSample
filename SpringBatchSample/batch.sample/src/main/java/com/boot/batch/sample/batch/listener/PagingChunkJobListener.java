package com.boot.batch.sample.batch.listener;

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
    public void afterJob(JobExecution jobExecution) {
        /*try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        log.info("PagingExampleChunkJobConfiguration end");
    }
}

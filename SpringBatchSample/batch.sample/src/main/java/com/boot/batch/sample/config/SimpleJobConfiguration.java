package com.boot.batch.sample.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SimpleJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;

    @Bean
    public Job simpleJob(){
        return jobBuilderFactory.get("simpleJob")
                .start(simpleStep1())
                .next(simpleStep2())
                .build();
    }

    @Bean
    public Step simpleStep1(){
        return stepBuilderFactory.get("simpleStep1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>This is Step1");
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().put("param1","parameterTest");
                    log.info("step1=>step2 parameter test / param1 : {}",
                            chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("param1"));
                    /*
                    chunkContext.getStepContext().getStepExecution().getExecutionContext().put("param1","parameterTest");
                    log.info("step1=>step2 parameter test / param1 : {}",
                            chunkContext.getStepContext().getStepExecution().getExecutionContext().get("param1"));
                     */
                    Long jobInstanceId = chunkContext.getStepContext().getJobInstanceId();
                    JobInstance jobInstance = jobExplorer.getJobInstance(jobInstanceId);

                    log.info("jobInstanceId : {} / jobInstance : {}", jobInstanceId, jobInstance);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step simpleStep2(){
        return stepBuilderFactory.get("simpleStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>This is Step2");
                    //String param = (String) chunkContext.getStepContext().getStepExecution().getExecutionContext().get("param1");
                    String param = (String) chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().get("param1");
                    log.info("step1=>step2 parameter test / param1 : {}", param);
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
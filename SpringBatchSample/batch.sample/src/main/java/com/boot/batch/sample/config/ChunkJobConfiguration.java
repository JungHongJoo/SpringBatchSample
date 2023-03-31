package com.boot.batch.sample.config;

import com.boot.batch.sample.Vo.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChunkJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final JobExplorer jobExplorer;

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job chunkJob(){
        return jobBuilderFactory.get("chunkJob")
                .start(chunkStep1())
                .build();
    }

    @Bean
    public Step chunkStep1(){
        return stepBuilderFactory.get("chunkStep1")
                .<Member, Member>chunk(CHUNK_SIZE)
                .reader(chunkReaderStep())
                .processor(chunkProcessorStep())
                .writer(chunkWriterStep())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<Member> chunkReaderStep(){
        return new JdbcCursorItemReaderBuilder<Member>()
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(Member.class))
                .sql("SELECT MEMBER_ID as memberId, MEMBER_NAME as memberName FROM MEMBER")
                .name("chunkReaderStepItemReader")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Member,Member> chunkProcessorStep(){
        return member -> {
            log.info("processor : memberId > {} / memberName > {}", member.getMemberId(), member.getMemberName());
            return member;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Member> chunkWriterStep(){
        return list -> {
            log.info("writer count : {}", list.size());
        };
    }

    @Bean
    public Step chunkStep2(){
        return stepBuilderFactory.get("chunkStep2")
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
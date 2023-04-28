package com.boot.batch.sample.config;

import com.boot.batch.sample.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CursorExampleChunkJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    private static final int CHUNK_SIZE = 100;

    @Bean
    public Job cursorExampleChunkJob() throws Exception{
        return jobBuilderFactory.get("cursorExampleChunkJob")
                .start(cursorExampleChunkStep())
                .build();
    }

    @Bean
    public Step cursorExampleChunkStep() throws Exception{
        return stepBuilderFactory.get("cursorExampleChunkStep")
                .<MemberDTO, MemberDTO>chunk(CHUNK_SIZE)
                .reader(cursorExampleChunkStepReader())
                .processor(cursorExampleChunkStepProcessor())
                .writer(cursorExampleChunkStepWriter())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<MemberDTO> cursorExampleChunkStepReader(){
        return new JdbcCursorItemReaderBuilder<MemberDTO>()
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(MemberDTO.class))
                .sql("SELECT MEMBER_ID , MEMBER_NAME , MEMBER_FLAG FROM MEMBER_INFO")
                .name("cursorExampleChunkStepItemReader")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<MemberDTO, MemberDTO> cursorExampleChunkStepProcessor(){
        return memberDTO -> {
            log.info("processor : memberId > {} / memberName > {}", memberDTO.getMemberId(), memberDTO.getMemberName());
            //Thread.sleep(10000);
            //log.info("Thread Sleep End");
            return memberDTO;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<MemberDTO> cursorExampleChunkStepWriter(){
        return list -> {
            log.info("writer count : {}", list.size());
        };
    }
}
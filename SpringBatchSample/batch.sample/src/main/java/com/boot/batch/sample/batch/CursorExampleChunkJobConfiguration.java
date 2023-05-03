package com.boot.batch.sample.batch;

import com.boot.batch.sample.batch.listener.CursorChunkJobListener;
import com.boot.batch.sample.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${spring.batch.job.chunk-size}")
    private int chunkSize;

    @Bean
    public Job cursorExampleChunkJob() throws Exception{
        return jobBuilderFactory.get("cursorExampleChunkJob")
                .listener(new CursorChunkJobListener())
                .start(cursorExampleChunkStep())
                .build();
    }

    @Bean
    public Step cursorExampleChunkStep() throws Exception{
        return stepBuilderFactory.get("cursorExampleChunkStep")
                .<MemberDTO, MemberDTO>chunk(chunkSize)
                .reader(cursorExampleChunkStepReader())
                .processor(cursorExampleChunkStepProcessor())
                .writer(cursorExampleChunkStepWriter())
                .build();
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<MemberDTO> cursorExampleChunkStepReader(){
        return new JdbcCursorItemReaderBuilder<MemberDTO>()
                .fetchSize(chunkSize)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(MemberDTO.class))
                .sql("SELECT MEMBER_ID , MEMBER_NAME FROM MEMBER_INFO WHERE MEMBER_FLAG_CURSOR = 'N'")
                .name("cursorExampleChunkStepItemReader")
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<MemberDTO, MemberDTO> cursorExampleChunkStepProcessor(){
        return memberDTO -> {
            memberDTO.setMemberName(memberDTO.getMemberName()+"ChunkCursor");
            return memberDTO;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<MemberDTO> cursorExampleChunkStepWriter(){
        return list -> {
            //log.info("writer count : {}", list.size());
        };
    }
}
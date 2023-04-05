package com.boot.batch.sample.config;

import com.boot.batch.sample.Dto.MemberDTO;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class TaskletJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final JobExplorer jobExplorer;

    private final JdbcTemplate jdbcTemplate;

    private static final String histInsertSql = "INSERT INTO MEMBER_READ_HIST (MEMBER_ID) VALUES (?)";

    @Bean
    public Job taskletJob(){
        return jobBuilderFactory.get("taskletJob")
                .start(taskletStep1())
                //.next(taskletStep2())
                .build();
    }

    @Bean
    public Step taskletStep1(){
        return stepBuilderFactory.get("taskletStep1")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>This is Step1");

                    /////////////////READER/////////////////
                    List<MemberDTO> memberList = jdbcTemplate.query(
                            "SELECT MEMBER_ID memberId, MEMBER_NAME memberName, MEMBER_FLAG memberFlag FROM MEMBER_INFO",
                            new BeanPropertyRowMapper<>(MemberDTO.class));


                    /////////////////PROCESSOR/////////////////
                    for(int i = 0 ; i < memberList.size() ; i++){
                        if(memberList.get(i).getMemberId() % 2 == 0){
                            log.info("[Remove Data]memberId : {} / memberName : {} / memberFlag : {}",
                                    memberList.get(i).getMemberId(), memberList.get(i).getMemberName(), memberList.get(i).getMemberFlag());
                            memberList.remove(i);
                        }
                    }


                    /////////////////WRITER/////////////////
                    int[] result = jdbcTemplate.batchUpdate(histInsertSql, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            MemberDTO memberDTO = memberList.get(i);
                            ps.setLong(1, memberDTO.getMemberId());
                        }

                        @Override
                        public int getBatchSize() {
                            return memberList.size();
                        }
                    });

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step taskletStep2(){
        return stepBuilderFactory.get("taskletStep2")
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
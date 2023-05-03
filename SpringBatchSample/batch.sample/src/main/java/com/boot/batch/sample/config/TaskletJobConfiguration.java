package com.boot.batch.sample.config;

import com.boot.batch.sample.config.listener.TaskletJobListener;
import com.boot.batch.sample.dto.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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

    private final JdbcTemplate jdbcTemplate;

    private static final String MEMBER_INFO_SELECT_SQL = "SELECT MEMBER_ID memberId, MEMBER_NAME memberName FROM MEMBER_INFO WHERE MEMBER_FLAG_TASKLET = 'N'";
    private static final String MEMBER_INFO_UPDATE_SQL = "UPDATE MEMBER_INFO SET MEMBER_FLAG_TASKLET = ? WHERE MEMBER_ID = ?";
    private static final String HIST_INSERT_SQL = "INSERT INTO MEMBER_READ_HIST_TASKLET (MEMBER_ID, MEMBER_NAME) VALUES (?, ?)";

    @Bean
    public Job taskletJob(){
        return jobBuilderFactory.get("taskletJob")
                .listener(new TaskletJobListener())
                .start(taskletStep1())
                //.next(taskletStep2())
                .build();
    }

    @Bean
    public Step taskletStep1(){
        return stepBuilderFactory.get("taskletStep1")
                .tasklet((contribution, chunkContext) -> {
                    //log.info(">>>>>>This is Step1");

                    /////////////////READER/////////////////
                    List<MemberDTO> memberList = jdbcTemplate.query(
                            MEMBER_INFO_SELECT_SQL,
                            new BeanPropertyRowMapper<>(MemberDTO.class));


                    /////////////////PROCESSOR/////////////////
                    for(int i = 0 ; i < memberList.size() ; i++){
                        memberList.get(i).setMemberName(memberList.get(i).getMemberName()+"Tasklet");
                    }


                    /////////////////WRITER/////////////////
                    int[] resultInsert = jdbcTemplate.batchUpdate(HIST_INSERT_SQL, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            MemberDTO memberDTO = memberList.get(i);
                            ps.setLong(1, memberDTO.getMemberId());
                            ps.setString(2, memberDTO.getMemberName());
                        }

                        @Override
                        public int getBatchSize() {
                            return memberList.size();
                        }
                    });

                    int[] resultUpdate = jdbcTemplate.batchUpdate(MEMBER_INFO_UPDATE_SQL, new BatchPreparedStatementSetter() {
                        @Override
                        public void setValues(PreparedStatement ps, int i) throws SQLException {
                            MemberDTO memberDTO = memberList.get(i);
                            ps.setString(1, "Y");
                            ps.setLong(2, memberDTO.getMemberId());
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
                    //log.info(">>>>>>This is Step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
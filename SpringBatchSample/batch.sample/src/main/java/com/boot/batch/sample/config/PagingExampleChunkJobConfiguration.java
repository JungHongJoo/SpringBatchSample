package com.boot.batch.sample.config;

import com.boot.batch.sample.Dto.MemberDTO;
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
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PagingExampleChunkJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;
    private final JobExplorer jobExplorer;

    private static final int CHUNK_SIZE = 100;
    private static final int THREAD_POOL_SIZE = 1;

    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(THREAD_POOL_SIZE);
        executor.setMaxPoolSize(THREAD_POOL_SIZE);
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public Job pagingExampleChunkJob() throws Exception{
        return jobBuilderFactory.get("pagingExampleChunkJob")
                .start(pagingExampleChunkStep())
                .build();
    }

    @Bean
    public Step pagingExampleChunkStep() throws Exception{
        return stepBuilderFactory.get("pagingExampleChunkStep")
                .<MemberDTO, MemberDTO>chunk(CHUNK_SIZE)
                .reader(pagingExampleChunkStepReader())
                .processor(pagingExampleChunkStepProcessor())
                .writer(pagingExampleChunkStepWriter())
                .taskExecutor(executor())
                .throttleLimit(THREAD_POOL_SIZE)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberDTO> pagingExampleChunkStepReader() throws Exception{
        return new JdbcPagingItemReaderBuilder<MemberDTO>()
                .fetchSize(CHUNK_SIZE)
                .dataSource(dataSource)
                .rowMapper(new BeanPropertyRowMapper<>(MemberDTO.class))
                .queryProvider(createQueryProvider())
                .name("pagingExampleChunkStepItemReader")
                .saveState(false)
                .build();
    }

    @Bean
    public PagingQueryProvider createQueryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource);
        queryProvider.setSelectClause("SELECT " +
                "MEMBER_ID memberId," +
                "MEMBER_NAME memberName," +
                "MEMBER_FLAG memberFlag");
        queryProvider.setFromClause("MEMBER_INFO");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("MEMBER_ID", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    @StepScope
    public ItemProcessor<MemberDTO, MemberDTO> pagingExampleChunkStepProcessor(){
        return memberDTO -> {
            log.info("processor : memberId > {} / memberName > {}", memberDTO.getMemberId(), memberDTO.getMemberName());
            if(memberDTO.getMemberId() % 2 != 0){
                log.info("This member ID is odd : {}", memberDTO.getMemberId());
                return null;
            }
            //Thread.sleep(10000);
            //log.info("Thread Sleep End");
            return memberDTO;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<MemberDTO> pagingExampleChunkStepWriter(){
        /*return list -> {
            log.info("writer count : {}", list.size());
        };*/
        return new JdbcBatchItemWriterBuilder<MemberDTO>()
                .dataSource(dataSource)
                .sql("insert into MEMBER_READ_HIST(MEMBER_ID) values (:memberId)")
                .beanMapped()
                .build();
    }

    @Bean
    public Step pagingExampleChunkStep2(){
        return stepBuilderFactory.get("pagingExampleChunkStep2")
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>>>This is Step2");

                    return RepeatStatus.FINISHED;
                })
                .build();
    }
}
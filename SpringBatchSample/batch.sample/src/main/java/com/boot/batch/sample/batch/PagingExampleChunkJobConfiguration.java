package com.boot.batch.sample.batch;

import com.boot.batch.sample.batch.listener.PagingChunkJobListener;
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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class PagingExampleChunkJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource dataSource;

    @Value("${spring.batch.job.chunk-size}")
    private int chunkSize;

    @Value("${spring.batch.job.thread-pool}")
    private int threadPoolCount;


    @Bean
    public TaskExecutor executor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadPoolCount);
        executor.setMaxPoolSize(threadPoolCount);
        executor.setThreadNamePrefix("multi-thread-");
        executor.setWaitForTasksToCompleteOnShutdown(Boolean.TRUE);
        executor.initialize();
        return executor;
    }

    @Bean
    public Job pagingExampleChunkJob() throws Exception{
        return jobBuilderFactory.get("pagingExampleChunkJob")
                .listener(new PagingChunkJobListener())
                .start(pagingExampleChunkStep())
                //.next()
                .build();
    }

    @Bean
    public Step pagingExampleChunkStep() throws Exception{
        return stepBuilderFactory.get("pagingExampleChunkStep")
                .<MemberDTO, MemberDTO>chunk(chunkSize)
                .reader(pagingExampleChunkStepReader())
                .processor(pagingExampleChunkStepProcessor())
                .writer(pagingExampleChunkStepWriter())
                .taskExecutor(executor())
                .throttleLimit(threadPoolCount)
                .build();
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<MemberDTO> pagingExampleChunkStepReader() throws Exception{
        return new JdbcPagingItemReaderBuilder<MemberDTO>()
                .fetchSize(chunkSize)
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
                "MEMBER_NAME memberName");
        queryProvider.setFromClause("MEMBER_INFO");
        queryProvider.setWhereClause("MEMBER_FLAG_PAGING = 'N'");

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("MEMBER_ID", Order.ASCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }

    @Bean
    @StepScope
    public ItemProcessor<MemberDTO, MemberDTO> pagingExampleChunkStepProcessor(){
        return memberDTO -> {
            /*log.info("[Processor] memberId : {} / memberName : {}", memberDTO.getMemberId(), memberDTO.getMemberName());
            Thread.sleep(3000);*/
            memberDTO.setMemberName(memberDTO.getMemberName()+"ChunkPaging");
            return memberDTO;
        };
    }

    @Bean
    @StepScope
    public ItemWriter<MemberDTO> pagingExampleChunkStepWriter(){
        return new MyItemWriter();
    }

    private class MyItemWriter implements ItemWriter<MemberDTO> {
        private final JdbcBatchItemWriter<MemberDTO> itemWriterHistUpdate;
        private final JdbcBatchItemWriter<MemberDTO> itemWriterInfoUpdate;

        private MyItemWriter() {
            this.itemWriterHistUpdate = new JdbcBatchItemWriterBuilder<MemberDTO>()
                    .dataSource(dataSource)
                    .sql("INSERT INTO MEMBER_READ_HIST_PAGING(MEMBER_ID, MEMBER_NAME) VALUES (:memberId, :memberName)")
                    .beanMapped()
                    .build();
            itemWriterHistUpdate.afterPropertiesSet();

            this.itemWriterInfoUpdate = new JdbcBatchItemWriterBuilder<MemberDTO>()
                    .dataSource(dataSource)
                    .sql("UPDATE MEMBER_INFO SET MEMBER_FLAG_PAGING = 'Y' WHERE MEMBER_ID = :memberId")
                    .beanMapped()
                    .build();
            itemWriterInfoUpdate.afterPropertiesSet();
        }

        @Override
        public void write(List<? extends MemberDTO> items) throws Exception {
            //log.info("writer count : {}", items.size());
            itemWriterHistUpdate.write(items);
            itemWriterInfoUpdate.write(items);
        }
    }

}
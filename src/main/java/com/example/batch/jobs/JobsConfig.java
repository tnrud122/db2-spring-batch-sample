package com.example.batch.jobs;

import com.example.batch.domain.TargetTxnRecord;
import com.example.batch.domain.TxnRecord;
import com.example.batch.mapper.target.TargetAggMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JobsConfig {

    // ---------------------------
    // 1) Tasklet Job (집계) - MyBatis 사용
    // ---------------------------
    @Bean
    public Job taskletAggJob(JobRepository jobRepository, Step taskletAggStep) {
        return new JobBuilder(JobNames.TASKLET_AGG_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(taskletAggStep)
                .build();
    }

    @Bean
    public Step taskletAggStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            TargetAggMapper targetAggMapper
    ) {
        return new org.springframework.batch.core.step.builder.StepBuilder("taskletAggStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    targetAggMapper.upsertYesterdayAgg();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // ---------------------------
    // 2) Chunk Job (대용량 이관) - MyBatis Reader/Writer + 멀티스레드
    // ---------------------------
    @Bean
    public Job chunkTransferJob(JobRepository jobRepository, Step chunkTransferStep) {
        return new JobBuilder(JobNames.CHUNK_TRANSFER_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(chunkTransferStep)
                .build();
    }

    @Bean
    public Step chunkTransferStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            SynchronizedItemStreamReader<TxnRecord> synchronizedReader,
            ItemProcessor<TxnRecord, TargetTxnRecord> txnProcessor,
            MyBatisBatchItemWriter<TargetTxnRecord> txnWriter,
            TaskExecutor batchTaskExecutor,
            @Value("${app.thread.concurrency:8}") int concurrency,
            @Value("${app.chunk.size:100}") int chunkSize
    ) {
        return new org.springframework.batch.core.step.builder.StepBuilder("chunkTransferStep", jobRepository)
                .<TxnRecord, TargetTxnRecord>chunk(chunkSize, transactionManager)
                .reader(synchronizedReader)
                .processor(txnProcessor)
                .writer(txnWriter)
                .taskExecutor(batchTaskExecutor)
                .throttleLimit(concurrency)
                .build();
    }

    /**
     * 멀티스레드 Step에서는 Reader가 thread-safe여야 합니다.
     * 가장 단순한 방법 중 하나는 SynchronizedItemStreamReader로 감싸는 것입니다.
     */
    @Bean
    public SynchronizedItemStreamReader<TxnRecord> synchronizedReader(MyBatisPagingItemReader<TxnRecord> delegate) {
        var sync = new SynchronizedItemStreamReader<TxnRecord>();
        sync.setDelegate(delegate);
        return sync;
    }

    @Bean
    public MyBatisPagingItemReader<TxnRecord> txnReader(
            @Qualifier("sourceSqlSessionFactory") SqlSessionFactory sourceSqlSessionFactory,
            @Value("${app.chunk.maxItems:10000}") int maxItems,
            @Value("${app.chunk.size:100}") int pageSize
    ) {
        Map<String, Object> params = new HashMap<>();
        params.put("maxId", maxItems);

        // MyBatisPagingItemReader는 MyBatis에서 페이징 파라미터(_page/_pagesize/_skiprows)를 제공 citeturn0search0turn0search3
        return new MyBatisPagingItemReaderBuilder<TxnRecord>()
                .sqlSessionFactory(sourceSqlSessionFactory)
                .queryId("com.example.batch.mapper.source.SourceTxnMapper.selectPaged")
                .parameterValues(params)
                .pageSize(pageSize)
                .saveState(false)
                .build();
    }

    @Bean
    public ItemProcessor<TxnRecord, TargetTxnRecord> txnProcessor() {
        return item -> new TargetTxnRecord(
                item.id(),
                item.customerNm(),
                item.amount(),
                item.createdAt(),
                LocalDateTime.now()
        );
    }

    @Bean
    public MyBatisBatchItemWriter<TargetTxnRecord> txnWriter(
            @Qualifier("targetSqlSessionFactory") SqlSessionFactory targetSqlSessionFactory
    ) {
        return new MyBatisBatchItemWriterBuilder<TargetTxnRecord>()
                .sqlSessionFactory(targetSqlSessionFactory)
                .statementId("com.example.batch.mapper.target.TargetTxnMapper.insertTxn")
                .build();
    }

    @Bean
    public TaskExecutor batchTaskExecutor(@Value("${app.thread.concurrency:8}") int concurrency) {
        SimpleAsyncTaskExecutor exec = new SimpleAsyncTaskExecutor("batch-");
        exec.setConcurrencyLimit(concurrency);
        return exec;
    }
}

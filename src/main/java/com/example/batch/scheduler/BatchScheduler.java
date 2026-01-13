package com.example.batch.scheduler;

import com.example.batch.jobs.JobNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(BatchScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job taskletAggJob;
    private final Job chunkTransferJob;

    public BatchScheduler(
            JobLauncher jobLauncher,
            @Qualifier(JobNames.TASKLET_AGG_JOB) Job taskletAggJob,
            @Qualifier(JobNames.CHUNK_TRANSFER_JOB) Job chunkTransferJob
    ) {
        this.jobLauncher = jobLauncher;
        this.taskletAggJob = taskletAggJob;
        this.chunkTransferJob = chunkTransferJob;
    }

    @Scheduled(cron = "${app.schedule.cron}", zone = "Asia/Seoul")
    public void runDailyAt5am() throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addString("runAt", LocalDateTime.now().toString())
                .toJobParameters();

        log.info("Starting jobs at 05:00 schedule. params={}", params);

        JobExecution aggExec = jobLauncher.run(taskletAggJob, params);
        log.info("taskletAggJob status={}", aggExec.getStatus());

        JobExecution chunkExec = jobLauncher.run(chunkTransferJob, params);
        log.info("chunkTransferJob status={}", chunkExec.getStatus());
    }
}

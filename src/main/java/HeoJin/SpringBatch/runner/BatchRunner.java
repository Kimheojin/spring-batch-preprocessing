package HeoJin.SpringBatch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchRunner {

    private final JobLauncher jobLauncher;


    public void runJob(Job job, JobParameters params) throws Exception {
        log.info("Job 실행 시작: {}", job.getName());

        if (params == null) {
            params = new JobParametersBuilder()
                    .addString("version", "1")
                    .toJobParameters();
        }


        jobLauncher.run(job, params);
        log.info("Job 실행 완료: {}", job.getName());
    }


    public void runJobWithTag(Job job, String tag) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("tag", tag)
                .toJobParameters();

        runJob(job, jobParameters);
    }


    public void runJob(Job job) throws Exception {
        runJob(job, null);
    }
}

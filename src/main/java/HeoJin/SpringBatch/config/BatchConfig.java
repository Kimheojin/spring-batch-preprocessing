package HeoJin.SpringBatch.config;


import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing // Spring Batch 활성화 되는 듯 (// JobRepository, JobLauncher_ 자동 생성)
public class BatchConfig {

    // 실행 명령, launcher 등으로 Job 단위 실행 가능
//    @Bean
//    public Job test
}

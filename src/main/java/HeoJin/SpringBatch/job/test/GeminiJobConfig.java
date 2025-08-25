package HeoJin.SpringBatch.job.test;

import HeoJin.SpringBatch.config.gemini.Gemma3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GeminiJobConfig {
    // testGeminiJob

    private final Gemma3Service gemma3Service;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    @Order(1)
    public Job testGeminiJob() {
        return new JobBuilder("testGeminiJob", jobRepository)
                .start(geminiTestStep())
                .build();
    }

    @Bean
    public Step geminiTestStep() {
        return new StepBuilder("geminiTestStep", jobRepository)
                .tasklet(geminiTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet geminiTasklet() {
        return (contribution, chunkContext) -> {
            log.info("Gemini API 테스트 시작");
            
            String testPrompt = "안녕하세요. 간단한 테스트 메시지입니다.";
            String response = gemma3Service.generateContent(testPrompt);
            
            log.info("Gemini API 응답: {}", response);
            log.info("Gemini API 테스트 완료");
            
            return RepeatStatus.FINISHED;
        };
    }
}
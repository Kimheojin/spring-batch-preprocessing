package HeoJin.SpringBatch.job.test;

import HeoJin.SpringBatch.config.gemini.Gemma3Service;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Configuration
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(
        name = "batch.jobs.test.gemini.enabled",
        havingValue = "true"
)
public class GeminiJobConfig {


    private final Gemma3Service gemma3Service;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoTemplate mongoTemplate;

    @Value("${prompt.test}")
    private String testPrompt;
    @Value("${recipe.test.testDB}")
    private String testRawDB;
    @Value("${recipe.test.testProcessedDB}")
    private String processedRawDB;
    @Bean
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
            // 2개
            Query query = new Query().limit(2);

            List<RawRecipe> rawRecipes = mongoTemplate.find(query, RawRecipe.class, testRawDB);


            ObjectMapper objectMapper = new ObjectMapper();
            String recipesJson = objectMapper.writeValueAsString(rawRecipes);
            String prompt = testPrompt + recipesJson;

            log.info(prompt);


            String response = gemma3Service.generateContent(prompt);
            
            // JSON 응답에서 실제 데이터 부분만 추출
            JsonNode responseNode = objectMapper.readTree(response);
            String actualData = responseNode.get("candidates").get(0)
                                            .get("content").get("parts").get(0)
                                            .get("text").asText();
            
            // "```json\n"과 "\n```" 제거
            String cleanedData = actualData.replaceAll("```json\\n", "").replaceAll("\\n```", "");
            
            log.info("정제된 데이터: {}", cleanedData);
            
            // JSON 문자열을 ProcessedRecipe 리스트로 변환
            List<ProcessedRecipe> processedRecipes = objectMapper.readValue(
                cleanedData, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, ProcessedRecipe.class)
            );
            
            // processedRawDB 컬렉션에 저장
            for (ProcessedRecipe processedRecipe : processedRecipes) {
                mongoTemplate.save(processedRecipe, processedRawDB);
            }
            
            log.info("Processed data saved to collection: {}", processedRawDB);
            log.info("Gemini API 테스트 완료");
            
            return RepeatStatus.FINISHED;
        };
    }
}

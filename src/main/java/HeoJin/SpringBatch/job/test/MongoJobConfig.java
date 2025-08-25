package HeoJin.SpringBatch.job.test;

import HeoJin.SpringBatch.entity.rawData.RawRecipe;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoJobConfig {
    // tesetMongoJob

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoTemplate mongoTemplate;

    @Value("${recipe.sites.okitchen.collection-name}")
    private String testCollectionName;

    @Bean
    @Order(2)
    public Job testMongoJob() {
        return new JobBuilder("testMongoJob", jobRepository)
                .start(mongoTestStep())
                .build();
    }

    @Bean
    public Step mongoTestStep() {
        return new StepBuilder("mongoTestStep", jobRepository)
                .tasklet(mongoTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet mongoTasklet() {
        return (contribution, chunkContext) -> {
            log.info("MongoDB 연결 테스트 시작");
            
            Query query = new Query();
            RawRecipe one = mongoTemplate.findOne(query, RawRecipe.class, testCollectionName);

            if (one != null) {
                log.info("=== 조회된 RawRecipe 데이터 ===");
                log.info("ID: {}", one.getId());
                log.info("Recipe Name: {}", one.getRecipeName());
                log.info("Cooking Time: {}", one.getCookingTime());
                log.info("Source URL: {}", one.getSourceUrl());
                log.info("Site Index: {}", one.getSiteIndex());
                log.info("Crawled At: {}", one.getCrawledAt());
                log.info("Ingredients Count: {}", one.getIngredientList().size());
                log.info("Cooking Orders Count: {}", one.getCookingOrderList().size());
                
                if (!one.getIngredientList().isEmpty()) {
                    log.info("--- Ingredients ---");
                    one.getIngredientList().forEach(ingredient -> 
                        log.info("  - {}: {}", ingredient.getIngredient(), ingredient.getQuantity()));
                }
                
                if (!one.getCookingOrderList().isEmpty()) {
                    log.info("--- Cooking Orders ---");
                    one.getCookingOrderList().forEach(order -> 
                        log.info("  Step {}: {}", order.getStep(), order.getInstruction()));
                }
            } else {
                log.info("Collection이 비어있거나 데이터가 없습니다.");
            }

            log.info("MongoDB 연결 테스트 완료");
            
            return RepeatStatus.FINISHED;
        };
    }
}
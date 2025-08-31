package HeoJin.SpringBatch.job.recipeJob;

import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.job.recipeItemProcessor.GeminiRecipeProcessor;
import HeoJin.SpringBatch.job.recipeItemReader.MongoCursorItemReader;
import HeoJin.SpringBatch.job.recipeItemWriter.MongoRecipeWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RecipeJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoCursorItemReader mongoCursorItemReader;
    private final GeminiRecipeProcessor geminiRecipeProcessor;
    private final MongoRecipeWriter mongoRecipeWriter;

    @Bean
    public Job processDataJob() {
        return new JobBuilder("processDataJob", jobRepository)
                .start(processDataStep())
                .build();
    }

    @Bean
    public Step processDataStep() {
        return new StepBuilder("processDataStep", jobRepository)
                .<List<RawRecipe>, List<ProcessedRecipe>>chunk(10, transactionManager)
                .reader(mongoCursorItemReader)
                .processor(geminiRecipeProcessor)
                .writer(mongoRecipeWriter)
                .taskExecutor(new SyncTaskExecutor()) // 단일 쓰레드  동기 실행 강제
//                .startLimit(3) // Step 재시작 횟수 제한
                .build();
    }
}

package HeoJin.SpringBatch.job.recipeJob;

import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.job.exception.CustomException;
import HeoJin.SpringBatch.job.recipeItemProcessor.GeminiRecipeProcessor;
import HeoJin.SpringBatch.job.recipeItemReader.MongoPagingItemReader;
import HeoJin.SpringBatch.job.recipeItemWriter.MongoRecipeWriter;
import HeoJin.SpringBatch.job.recipeListener.ProcessorSkipListener;
import HeoJin.SpringBatch.job.recipeListener.ReaderSkipListener;
import HeoJin.SpringBatch.job.recipeListener.WriterSkipListener;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class RecipeJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MongoPagingItemReader mongoCursorItemReader;
    private final GeminiRecipeProcessor geminiRecipeProcessor;
    private final MongoRecipeWriter mongoRecipeWriter;
    private final ProcessorSkipListener processorSkipListener;
    private final ReaderSkipListener readerSkipListener;
    private final WriterSkipListener writerSkipListener;


    @Bean
    public Job processDataJob() {
        return new JobBuilder("processDataJob", jobRepository)
                .start(processDataStep())
                .build();
    }

    @Bean
    public Step processDataStep() {
        // 청크 방식으로
        return new StepBuilder("processDataStep", jobRepository)
                .<List<RawRecipe>, List<ProcessedRecipe>>chunk(10, transactionManager)
                .reader(mongoCursorItemReader)
                .processor(geminiRecipeProcessor)
                .writer(mongoRecipeWriter)
                .taskExecutor(new SyncTaskExecutor()) // 단일 쓰레드  동기 실행 강제
                // 장애 허용 (오류가 발생해도 일단 진행 허용)
                .faultTolerant()
                .skip(CustomException.class)
                .skipLimit(500)
                .listener(readerSkipListener)
                .listener(processorSkipListener)
                .listener(writerSkipListener)

                .build();
    }
}

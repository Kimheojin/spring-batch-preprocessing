package HeoJin.SpringBatch.job.dummyDataJob;

import HeoJin.SpringBatch.entity.dummyData.post.Post;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.job.dummyDataJob.InitStep.InitTasklet;
import HeoJin.SpringBatch.job.dummyDataJob.dummyDataProcessor.DummyDataProcessor;
import HeoJin.SpringBatch.job.dummyDataJob.dummyDataWriter.DummyDataWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.MongoCursorItemReader;
import org.springframework.batch.item.data.builder.MongoCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Configuration
public class DummyDataConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InitTasklet initTasklet;
    private final MongoTemplate mongoTemplate;
    private final DummyDataProcessor dummyDataProcessor;
    private final DummyDataWriter dummyDataWriter;

    @Value("${spring.data.mongodb.collectionName}")
    private String rawDataCollectionName;

    @Bean
    public Job dummyDataJob(){
        return new JobBuilder("dummyDataJob4", jobRepository)
                .start(initStep())
                .next(dummyDataStep())
                .build();
    }

    @Bean
    public Step initStep() {
        return new StepBuilder("initStep", jobRepository)
                .tasklet(initTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step dummyDataStep() {
        return new StepBuilder("dummyDataStep", jobRepository)
                .<RawRecipe, List<Post>>chunk(10, transactionManager)
                .reader(dummyDataReader())
                .processor(dummyDataProcessor)
                .writer(dummyDataWriter)
                .stream(dummyDataProcessor)
                .build();
    }

    @Bean
    public MongoCursorItemReader<RawRecipe> dummyDataReader() {
        return new MongoCursorItemReaderBuilder<RawRecipe>()
                .name("dummyDataReader")
                .template(mongoTemplate)
                .collection(rawDataCollectionName)
                .targetType(RawRecipe.class)
                .sorts(Collections.singletonMap("_id", Sort.Direction.ASC))
                .build();
    }
}

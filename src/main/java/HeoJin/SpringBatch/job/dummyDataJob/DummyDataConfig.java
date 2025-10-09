package HeoJin.SpringBatch.job.dummyDataJob;

import HeoJin.SpringBatch.entity.dummyData.Post;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.job.dummyDataJob.InitStep.InitTasklet;
import HeoJin.SpringBatch.job.dummyDataJob.dummyDataProcessor.DummyDataProcessor;
import HeoJin.SpringBatch.job.dummyDataJob.dummyDataReader.DummyDataReader;
import HeoJin.SpringBatch.job.dummyDataJob.dummyDataWriter.DummyDataWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class DummyDataConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final InitTasklet initTasklet;
    private final DummyDataReader dummyDataReader;
    private final DummyDataProcessor dummyDataProcessor;
    private final DummyDataWriter dummyDataWriter;

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
        //Processor가 ItemStream 인터페이스를 구현하면, Step에 명시적으로 등록해야됨
        return new StepBuilder("dummyDataStep", jobRepository)
                .<RawRecipe, List<Post>>chunk(100, transactionManager)
                .reader(dummyDataReader)
                .processor(dummyDataProcessor)
                .writer(dummyDataWriter)
                .stream(dummyDataReader)
                .stream(dummyDataProcessor)
                .build();
    }
}

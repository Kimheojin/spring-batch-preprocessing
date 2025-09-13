package HeoJin.SpringBatch.job.recipeListener;

import HeoJin.SpringBatch.entity.BatchError.BatchError;
import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.repository.BatchErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.util.List;


@RequiredArgsConstructor
@Component
@Slf4j
public class WriterSkipListener implements SkipListener<List<RawRecipe>, List<ProcessedRecipe>> {

    // writer  전용
    private final BatchErrorRepository batchErrorRepository;
    @Override
    public void onSkipInWrite(List<ProcessedRecipe> items, Throwable t) {

        log.warn("onSkipWrite 진입 : {}", t.getMessage());

        for (ProcessedRecipe item : items) {

            BatchError batchError = BatchError.builder()
                    .sourceUrl(item.getSourceUrl())
                    .siteIndex(item.getSiteIndex())
                    .build();
            batchErrorRepository.save(batchError);
            log.error("writer 오류 : {}, {}", batchError.getSourceUrl(), batchError.getSiteIndex());
        }

    }


}

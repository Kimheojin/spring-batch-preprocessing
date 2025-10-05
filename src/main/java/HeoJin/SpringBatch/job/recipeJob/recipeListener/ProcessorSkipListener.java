package HeoJin.SpringBatch.job.recipeJob.recipeListener;


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
public class ProcessorSkipListener implements SkipListener<List<RawRecipe>, List<ProcessedRecipe>> {

    private final BatchErrorRepository batchErrorRepository;
    // 프로세서 전용

    @Override
//    @Transactional 이거 애매하다
    public void onSkipInProcess(List<RawRecipe> items, Throwable t) {
        log.warn("onSkipInProcess 진입 : {}", t.getMessage());
        if (items == null || items.isEmpty()) {
            log.error("onSkipProcess : items 존재 X");
            return;
        }

        for (RawRecipe item : items) {
            BatchError batchError = BatchError.builder()
                    .sourceUrl(item.getSourceUrl())
                    .siteIndex(item.getSiteIndex())
                    .build();
            batchErrorRepository.save(batchError);
            log.error("onSkipProcess 에러 :{}, {}", batchError.getSourceUrl(), batchError.getSiteIndex());
        }
    }



}

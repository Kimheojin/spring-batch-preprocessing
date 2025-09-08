package HeoJin.SpringBatch.job.recipeListener;


import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProcessorSkipListener implements SkipListener<List<RawRecipe>, List<ProcessedRecipe>> {

    // 프로세서 전용

    @Override
    public void onSkipInProcess(List<RawRecipe> items, Throwable t) {
    }



}

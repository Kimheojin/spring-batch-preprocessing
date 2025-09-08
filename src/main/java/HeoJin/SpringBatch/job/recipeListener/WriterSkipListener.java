package HeoJin.SpringBatch.job.recipeListener;

import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class WriterSkipListener implements SkipListener<List<RawRecipe>, List<ProcessedRecipe>> {

    // writer  전용

    @Override
    public void onSkipInWrite(List<ProcessedRecipe> items, Throwable t) {

    }


}

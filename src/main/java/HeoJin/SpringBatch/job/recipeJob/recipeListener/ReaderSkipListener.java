package HeoJin.SpringBatch.job.recipeJob.recipeListener;


import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ReaderSkipListener implements SkipListener<List<RawRecipe>,List<ProcessedRecipe>> {

    @Override
    public void onSkipInRead(Throwable t) {
        log.error("onSkipInRead 발생 : {}", t.getMessage());
    }
}

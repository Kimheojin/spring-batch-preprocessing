package HeoJin.SpringBatch.job.recipeItemProcessor;


import HeoJin.SpringBatch.config.gemini.Gemma3Service;
import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiRecipeProcessor implements ItemProcessor<List<RawRecipe>, List<ProcessedRecipe>> {
    // I, O 순으로 지정
    private final Gemma3Service gemma3Service;

    @Override
    public List<ProcessedRecipe> process(List<RawRecipe> items) throws Exception {
        if (items == null || items.isEmpty()) {
            return List.of();
        }
        
        log.info("레시피 사지으 1개 or 2개 : {} 개", items.size());
        
        // Gemini로 배치 처리
        List<ProcessedRecipe> processedRecipes = gemma3Service.processBatch(items);
        
        log.info("Successfully processed {} recipes", processedRecipes.size());
        
        // API rate limit 대응
        Thread.sleep(30000);

        
        return processedRecipes;
    }
}

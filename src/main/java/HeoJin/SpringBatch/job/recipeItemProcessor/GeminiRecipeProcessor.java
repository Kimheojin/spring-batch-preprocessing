package HeoJin.SpringBatch.job.recipeItemProcessor;


import HeoJin.SpringBatch.config.gemini.Gemma3Service;
import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.job.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiRecipeProcessor implements ItemProcessor<List<RawRecipe>, List<ProcessedRecipe>> {
    // I, O 순으로 지정
    private final Gemma3Service gemma3Service;

    @Value("${prompt.test}")
    private String testPrompt;

    @Override
    public List<ProcessedRecipe> process(List<RawRecipe> items) throws Exception {
        // items는 2개씩 묶어서

        if (items == null || items.isEmpty()) {
            throw new CustomException("Reader에서 빈 item이 넘어옴");
        }
        
        log.info("레시피 사지으 1개 or 2개 : {} 개", items.size());
        
        // Gemini로 2개씩
        List<ProcessedRecipe> processedRecipes = gemma3Service.processBatch(items, testPrompt);
        
        log.info("Successfully processed {} recipes", processedRecipes.size());
        
        // API rate limit 대응
        Thread.sleep(30000);

        
        return processedRecipes;
    }
}

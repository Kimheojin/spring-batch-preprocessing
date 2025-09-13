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

        if(items.size() != 2) {
            throw new CustomException("예상과 다른 배치 사이즈");
        }

        // cooking order list 없으면 복구 불가 형태
        if ( items.get(0).getCookingOrderList().isEmpty()
                || items.get(1).getCookingOrderList().isEmpty()  ) {
            throw new CustomException("불완전한 데이터 넘어옴");
        }

        log.info("레시피  2개 : {} 개", items.size());
        
        // Gemini로 2개씩
        List<ProcessedRecipe> processedRecipes = gemma3Service.processBatch(items, testPrompt);
        
        log.info("Successfully processed {} recipes", processedRecipes.size());



        // API rate limit 대응
        Thread.sleep(30000);

        
        return processedRecipes;
    }
}

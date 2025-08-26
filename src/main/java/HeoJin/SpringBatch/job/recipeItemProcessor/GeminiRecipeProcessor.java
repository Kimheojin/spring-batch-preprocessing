package HeoJin.SpringBatch.job.recipeItemProcessor;


import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GeminiRecipeProcessor implements ItemProcessor<RawRecipe, ProcessedRecipe> {
}

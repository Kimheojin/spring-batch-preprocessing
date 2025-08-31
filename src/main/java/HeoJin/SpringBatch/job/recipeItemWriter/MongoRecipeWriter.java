package HeoJin.SpringBatch.job.recipeItemWriter;

import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class MongoRecipeWriter implements ItemStreamWriter<List<ProcessedRecipe>> {
    
    private final MongoTemplate mongoTemplate;

    @Value("${recipe.deploy.processedDB}")
    private String processedCollectionName;
    
    @Override
    public void write(Chunk<? extends List<ProcessedRecipe>> chunk) throws Exception {
        // List<List<ProcessedRecipe>> -> 이거 청크단위로 묶여서 내려옴
        List<ProcessedRecipe> flattenedRecipes = chunk.getItems()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
        if (!flattenedRecipes.isEmpty()) {
            mongoTemplate.insert(flattenedRecipes, processedCollectionName);
            log.info("Saved {} processed recipes to MongoDB", flattenedRecipes.size());
        }
    }
}

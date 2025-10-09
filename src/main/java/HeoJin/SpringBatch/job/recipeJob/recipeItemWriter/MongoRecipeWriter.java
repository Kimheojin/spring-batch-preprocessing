package HeoJin.SpringBatch.job.recipeJob.recipeItemWriter;

import HeoJin.SpringBatch.entity.processedData.ProcessedRecipe;
import HeoJin.SpringBatch.job.exception.CustomException;
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

        List<ProcessedRecipe> validRecipes = chunk.getItems()
                .stream()
                .flatMap(List::stream)
                .filter(this::isValidRecipe) // this 는 현재 클래스
                .collect(Collectors.toList());

        // 검증용
        int totalRecipeCount = chunk.getItems()
                .stream()
                .mapToInt(List::size)
                .sum();


        if(validRecipes.size() != totalRecipeCount) {
            throw new CustomException("writer 부분 불완전 데이터");
        }
        


        mongoTemplate.insert(validRecipes, processedCollectionName);
        log.info("저장 완료 {} 개", validRecipes.size());

        // 다른 옵션 link들
        // https://docs.spring.io/spring-data/mongodb/docs/current/api/org/springframework/data/mongodb/core/BulkOperations.html
        // https://pasudo123.tistory.com/504
        // 나중에 고려하기ㅣ(아직은 데이터 테스트 부족)
    }

    private boolean isValidRecipe(ProcessedRecipe recipe) {
        if (recipe == null) return false;
        
        // 필수 필드들이 비어있으면 필터링
        if (isEmpty(recipe.getRecipeName()) || 
            isEmpty(recipe.getSourceUrl()) || 
            isEmpty(recipe.getSiteIndex()) ||
            recipe.getIngredientList() == null || recipe.getIngredientList().isEmpty() ||
            recipe.getCookingOrderList() == null || recipe.getCookingOrderList().isEmpty()) {

            log.warn("필드 비어잇음: {}", recipe.getId());
            return false;
        }
        
        return true;
    }
    
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}

package HeoJin.SpringBatch.entity.processedData;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProcessedRecipe {
    @Id
    private String id;

    private String recipeName;
    private String sourceUrl;
    private String siteIndex;

    @Builder.Default
    private List<String> ingredientList = new ArrayList<>();
    @Builder.Default
    private List<ProcessedCookingOrder> cookingOrderList = new ArrayList<>();


}

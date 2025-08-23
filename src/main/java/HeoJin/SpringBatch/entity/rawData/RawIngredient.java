package HeoJin.SpringBatch.entity.rawData;


import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class Ingredient {
    private String ingredient;
    private String quantity;
}

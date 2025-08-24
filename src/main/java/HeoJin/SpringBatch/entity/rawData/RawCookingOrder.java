package HeoJin.SpringBatch.entity.rawData;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RawCookingOrder {

    private int step;
    private String instruction;
}

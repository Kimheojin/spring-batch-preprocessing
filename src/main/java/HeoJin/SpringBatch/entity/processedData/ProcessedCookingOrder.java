package HeoJin.SpringBatch.entity.processedData;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ProcessedCookingOrder {
    private int step;
    private string instruction;
}

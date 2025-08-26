package HeoJin.SpringBatch.job.recipeItemReader;


import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import com.mongodb.client.MongoCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoCursorItemReader implements ItemStreamReader<RawRecipe> {

    // ItemStream, ItemReader -> ItemStreamReader

    // SocketTimeout을 충분히 큰 값으로 설정 하기
    // https://jojoldu.tistory.com/336
    // https://docs.spring.io/spring-batch/reference/readers-and-writers/item-stream.html

    private static final String CURRENT_ITEM_COUNT = "current.item.count";

    private final MongoTemplate mongoTemplate;
    private MongoCursor<RawRecipe> mongoCursor;
    private int currentItemCount = 0;

    @Override
    public RawRecipe read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (mongoCursor == null) {
            return null;
        }

        if (mongoCursor.hasNext()) {
            currentItemCount++;
            return mongoCursor.next();
        }
        
        return null;
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        currentItemCount = executionContext.getInt(CURRENT_ITEM_COUNT, 0);
        
        Query query = new Query().skip(currentItemCount);
        mongoCursor = mongoTemplate.getCollection("rawRecipe")
                .find(RawRecipe.class)
                .cursor();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putInt(CURRENT_ITEM_COUNT, currentItemCount);
    }

    @Override
    public void close() throws ItemStreamException {
        if (mongoCursor != null) {
            mongoCursor.close();
        }
    }
}
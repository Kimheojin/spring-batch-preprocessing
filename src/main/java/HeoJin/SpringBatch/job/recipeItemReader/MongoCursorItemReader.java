package HeoJin.SpringBatch.job.recipeItemReader;


import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import com.mongodb.client.MongoCursor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MongoCursorItemReader implements ItemStreamReader<RawRecipe> {

    // ItemStream, ItemReader -> ItemStreamReader

    // SocketTimeout을 충분히 큰 값으로 설정 하기
    // https://jojoldu.tistory.com/336
    // https://docs.spring.io/spring-batch/reference/readers-and-writers/item-stream.html

    //https://www.mongodb.com/ko-kr/docs/manual/reference/method/cursor.batchSize/
    // 초기 배치 사이즈 101개

    private static final String LAST_PROCESSED_ID = "last.processed.id";

    private final MongoTemplate mongoTemplate;
    private MongoCursor<RawRecipe> mongoCursor;
    private String lastProcessedId;

    @Override
    public RawRecipe read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (mongoCursor == null || !mongoCursor.hasNext()) {
            return null;
        }

        RawRecipe item = mongoCursor.next();
        lastProcessedId = item.getId(); // 재시작용 저장
        return item;

    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        lastProcessedId = executionContext.getString(LAST_PROCESSED_ID, null);
        
        Query query = new Query();
        if (lastProcessedId != null) {
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(lastProcessedId)));
            log.info("Resuming from last processed ID: {}", lastProcessedId);
        }

        // 기본으로 생성되는 오름차순 인덱스 사용
        query.with(Sort.by(Sort.Direction.ASC, "_id"));

        mongoCursor = mongoTemplate.getCollection("test")
                .find(query.getQueryObject())
                .sort(query.getSortObject())
                .map(doc ->  mongoTemplate.getConverter()
                        .read(RawRecipe.class, doc))
                .cursor();

        ;

    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {

        // meta schema에 저장
        if (lastProcessedId != null) {
            executionContext.putString(LAST_PROCESSED_ID, lastProcessedId);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (mongoCursor != null) {
            mongoCursor.close();
            log.info("MongoDB cursor closed");
        }
    }
}
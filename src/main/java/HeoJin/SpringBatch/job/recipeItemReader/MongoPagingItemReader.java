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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MongoPagingItemReader implements ItemStreamReader<List<RawRecipe>> {

    // open(시작할 떄 한번) -> read -> update -> close
    private static final String LAST_PROCESSED_ID = "last.processed.id";
    private static final int PAIR_SIZE = 2; // 2개씩 묶어서 처리


    private final MongoTemplate mongoTemplate;
    private MongoCursor<RawRecipe> mongoCursor;
    private String lastProcessedId;


    @Value("${recipe.deploy.rawDB}")
    private String rawDataCollectionName;


    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // job 시작할 때 한번

        lastProcessedId = executionContext.getString(LAST_PROCESSED_ID, null);

        Query query = new Query();

        if (lastProcessedId != null) {
            query.addCriteria(Criteria.where("_id")
                    .gt(new ObjectId(lastProcessedId))); // 조건 추가
            log.info("현재 마지막 processedId : {}", lastProcessedId);
        }

        // 기본으로 생성되는 오름차순 인덱스 사용
        query.with(Sort.by(Sort.Direction.ASC, "_id")); // 명시적 정렬 조건 추가

        mongoCursor = mongoTemplate.getCollection(rawDataCollectionName)
                .find(query.getQueryObject())
                .sort(query.getSortObject())
                .batchSize(100) // 배치 사이즈 설정 (기본값: 101)
                .map(doc ->  mongoTemplate.getConverter()
                        .read(RawRecipe.class, doc))
                .cursor();



    }
    @Override
    public List<RawRecipe> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (mongoCursor == null || !mongoCursor.hasNext()) {
            return null;
        }

        List<RawRecipe> batch = new ArrayList<>();
        
        // 2개씩 묶어서 반환
        for (int i = 0; i < PAIR_SIZE && mongoCursor.hasNext(); i++) {
            RawRecipe item = mongoCursor.next();
            batch.add(item);
            lastProcessedId = item.getId(); // 마지막 아이템의 ID 저장

        }
        

        return batch.isEmpty() ? null : batch;
    }


    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // 커밋 성공 시에만 (청크 단위로)

        // meta schema에 저장
        if (lastProcessedId != null) {
            // Json 형태로 인코딩한 다음 저장
            executionContext.putString(LAST_PROCESSED_ID, lastProcessedId);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        // job 마지막에 한번
        if (mongoCursor != null) {
            mongoCursor.close();
            log.info("MongoDB cursor closed");
        }
    }
}

// ItemStream, ItemReader -> ItemStreamReader

// SocketTimeout을 충분히 큰 값으로 설정 하기
// https://jojoldu.tistory.com/336
// https://docs.spring.io/spring-batch/reference/readers-and-writers/item-stream.html
// https://www.mongodb.com/ko-kr/docs/drivers/java/sync/current/crud/query-documents/cursor/

//https://www.mongodb.com/ko-kr/docs/manual/reference/method/cursor.batchSize/
// 초기 배치 사이즈 101개
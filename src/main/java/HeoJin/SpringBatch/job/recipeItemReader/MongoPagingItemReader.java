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
    private static final int PAGE_SIZE = 100;


    private final MongoTemplate mongoTemplate;
    // 싱글스레드라 아직으 신경 X, 스레드 구조 바뀌면 바꾸기
    private List<RawRecipe> currentBatch;
    private String lastProcessedId;
    private int currentIndex;


    @Value("${recipe.deploy.rawDB}")
    private String rawDataCollectionName;


    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // job 시작할 때 한번
        lastProcessedId = executionContext.getString(LAST_PROCESSED_ID, null);

        currentBatch = new ArrayList<>();
        currentIndex = 0;
        if(lastProcessedId != null){
            log.info("재시작: 마지마가 처리된 ID: {}", lastProcessedId);
        }


    }
    @Override
    public List<RawRecipe> read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        // 현재 배치가 비어있거나 or 모두 처리했으면 새로 로드
        if (currentBatch.isEmpty() || currentIndex >= currentBatch.size()) {
            loadNextBatch();
            currentIndex = 0;
        }

        if(currentBatch.isEmpty()){
            // 다 읽은 경우
            return null;
        }

        List<RawRecipe> batch = new ArrayList<>();
        
        // pair 사이즈 만큼 묶어서 반환
        // current Index 0 부터 시작이라 < 로 처리
        for (int i = 0; i < PAIR_SIZE && currentIndex < currentBatch.size(); i++) {
            RawRecipe item = currentBatch.get(currentIndex++);
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
        currentBatch.clear();
        log.info("Mongo DB 페이징 리더 종료");
    }

    private void loadNextBatch() {
        Query query = new Query();

        if(lastProcessedId != null){
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(lastProcessedId)));
        }

        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.limit(PAGE_SIZE);

        // hint 메소드 사용
        query.withHint("_id_"); // 인덱스 이름으로 지정

        currentBatch = mongoTemplate.find(query, RawRecipe.class, rawDataCollectionName);
    }
}

// ItemStream, ItemReader -> ItemStreamReader

// SocketTimeout을 충분히 큰 값으로 설정 하기
// https://jojoldu.tistory.com/336
// https://docs.spring.io/spring-batch/reference/readers-and-writers/item-stream.html
// https://www.mongodb.com/ko-kr/docs/drivers/java/sync/current/crud/query-documents/cursor/

//https://www.mongodb.com/ko-kr/docs/manual/reference/method/cursor.batchSize/
// 초기 배치 사이즈 101개
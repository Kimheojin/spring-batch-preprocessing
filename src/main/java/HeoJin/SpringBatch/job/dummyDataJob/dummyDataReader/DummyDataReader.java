package HeoJin.SpringBatch.job.dummyDataJob.dummyDataReader;


import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.batch.item.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDataReader implements ItemStreamReader<RawRecipe> {

//  - open(): Step 시작 시 1회
//  - read(): chunk 크기만큼 반복 호출
//  - update(): 각 chunk 커밋마다 호출
//  - close(): Step 종료 시 1회

    // 커스텀 키
    private static final String LAST_PROCESSED_ID = "last.processed.id";
    private final MongoTemplate mongoTemplate;
    private String lastProcessedId;


    // open(job 시작할 떄 한번) -> read -> update -> close
    @Value("${recipe.deploy.rawDB}")
    private String rawDataCollectionName;

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        lastProcessedId = executionContext.getString(LAST_PROCESSED_ID, null);

        if(lastProcessedId != null){
            log.info("재시작: 마지막 처리된 ID: {}", lastProcessedId);
        }

    }

    @Override
    public RawRecipe read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        Query query = new Query();

        if(lastProcessedId != null){
            query.addCriteria(Criteria.where("_id").gt(new ObjectId(lastProcessedId)));
        }
        query.with(Sort.by(Sort.Direction.ASC, "_id"));
        query.withHint("_id_");
        query.limit(1);

        RawRecipe rawRecipe = mongoTemplate.findOne(query, RawRecipe.class, rawDataCollectionName);

        if(rawRecipe == null) {
            return null;  // 더 이상 읽을 데이터 없음 → chunk 종료
        }

        lastProcessedId = rawRecipe.getId();
        return rawRecipe;
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        // meta schema에 저장
        if (lastProcessedId != null) {
            // Json 형태로 인코딩한 다음 저장
            executionContext.putString(LAST_PROCESSED_ID, lastProcessedId);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        ItemStreamReader.super.close();
    }


}

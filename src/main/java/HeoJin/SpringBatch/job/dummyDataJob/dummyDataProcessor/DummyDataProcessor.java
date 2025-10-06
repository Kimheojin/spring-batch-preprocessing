package HeoJin.SpringBatch.job.dummyDataJob.dummyDataProcessor;

import HeoJin.SpringBatch.entity.dummyData.Category;
import HeoJin.SpringBatch.entity.dummyData.Member;
import HeoJin.SpringBatch.entity.dummyData.Post;
import HeoJin.SpringBatch.entity.rawData.RawCookingOrder;
import HeoJin.SpringBatch.entity.rawData.RawRecipe;
import HeoJin.SpringBatch.repository.CategoryRepository;
import HeoJin.SpringBatch.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDataProcessor implements ItemProcessor<RawRecipe, List<Post>>, ItemStream {
    private final CategoryRepository categoryRepository;
    private final MemberRepository memberRepository;

    private List<Category> categories;  // 메모리에 캐싱
    private Member defaultMember;       // 메모리에 캐싱
    private Random random = new Random();


    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // Step 시작 시 1회만 로딩
        categories = categoryRepository.findAll();
        defaultMember = memberRepository.findByEmail("hurjin1109@naver.com")
                .orElseThrow();
        log.info("카테고리 {}개, Member 로딩 완료", categories.size());
    }



    @Override
    public List<Post> process(RawRecipe item) throws Exception {
        List<RawCookingOrder> cookingOrderList = item.getCookingOrderList();

        List<Post> postList = new ArrayList<>();


        // 빈거 체크 안해도 괜찮지 않을까
        for(RawCookingOrder content : cookingOrderList){
            Category randomCategory = categories.get(random.nextInt(categories.size()));

            Post post = Post.builder()
                    .content(content.getInstruction())
                    .title(item.getRecipeName() + " - " + content.getStep())
                    .member(defaultMember)
                    .category(randomCategory)
                    .regDate(LocalDateTime.now())
                    .build();

            postList.add(post);
        }

        return postList;
    }

    @Override
    public void update(ExecutionContext executionContext) {}

    @Override
    public void close() {}

}

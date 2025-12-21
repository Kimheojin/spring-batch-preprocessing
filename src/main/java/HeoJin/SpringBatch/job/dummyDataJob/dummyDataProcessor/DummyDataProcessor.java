package HeoJin.SpringBatch.job.dummyDataJob.dummyDataProcessor;

import HeoJin.SpringBatch.entity.dummyData.category.Category;
import HeoJin.SpringBatch.entity.dummyData.member.Member;
import HeoJin.SpringBatch.entity.dummyData.post.Post;
import HeoJin.SpringBatch.entity.dummyData.post.PostStatus;
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
    private final HeoJin.SpringBatch.repository.TagRepository tagRepository;

    private static final List<PostStatus> POST_STATUSES = List.of(
            PostStatus.DRAFT,
            PostStatus.PUBLISHED,
            PostStatus.PRIVATE,
            PostStatus.SCHEDULED
    );

    private List<Category> categories;  // 메모리에 캐싱
    private List<Long> allTagIds;       // 태그 ID 캐싱
    private Member defaultMember;       // 메모리에 캐싱
    private Random random = new Random();


    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // Step 시작 시 1회만 로딩
        categories = categoryRepository.findAll();
        allTagIds = tagRepository.findAll().stream()
                .map(HeoJin.SpringBatch.entity.dummyData.tag.Tag::getId)
                .collect(java.util.stream.Collectors.toList());
        defaultMember = memberRepository.findByEmail("hurjin1109@naver.com")
                .orElseThrow();
        log.info("카테고리 {}개, 태그 {}개, Member 로딩 완료", categories.size(), allTagIds.size());
    }



    @Override
    public List<Post> process(RawRecipe item) throws Exception {
        List<RawCookingOrder> cookingOrderList = item.getCookingOrderList();

        List<Post> postList = new ArrayList<>();

        for(RawCookingOrder content : cookingOrderList){
            for(int i = 0; i < 100; i++) {
                Category randomCategory = categories.get(random.nextInt(categories.size()));
                PostStatus postStatus = POST_STATUSES.get(random.nextInt(POST_STATUSES.size()));
                
                // 랜덤 태그 선택 (0~3개)
                List<Long> selectedTagIds = new ArrayList<>();
                if (!allTagIds.isEmpty()) {
                    int tagCount = random.nextInt(4); // 0, 1, 2, 3
                    for (int k = 0; k < tagCount; k++) {
                        selectedTagIds.add(allTagIds.get(random.nextInt(allTagIds.size())));
                    }
                }

                Post post = Post.builder()
                        .content(content.getInstruction() + "추가본" + i)
                        .title(item.getRecipeName() + " - " + content.getStep() + "추가본" + i)
                        .member(defaultMember)
                        .category(randomCategory)
                        .regDate(LocalDateTime.now())
                        .status(postStatus)
                        .build();
                
                post.setTagIds(selectedTagIds);

                postList.add(post);
            }
        }

        return postList;
    }

    @Override
    public void update(ExecutionContext executionContext) {}

    @Override
    public void close() {}

}

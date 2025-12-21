package HeoJin.SpringBatch.job.dummyDataJob.dummyDataWriter;

import HeoJin.SpringBatch.entity.dummyData.post.Post;
import HeoJin.SpringBatch.entity.dummyData.tag.PostTag;
import HeoJin.SpringBatch.repository.PostRepository;
import HeoJin.SpringBatch.repository.PostTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DummyDataWriter implements ItemStreamWriter<List<Post>> {

    private final PostRepository postRepository;
    private final PostTagRepository postTagRepository;

    @Override
    public void write(Chunk<? extends List<Post>> chunk) throws Exception {
        // chunk 단편화 (List<List<Post>> -> List<Post>)
        List<Post> allPosts = chunk.getItems().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allPosts.isEmpty()) {
            return;
        }

        // JPA 사용: Post 저장 (ID 생성됨)
        // saveAll은 내부적으로 loop 돌면서 insert 하지만 트랜잭션 안에서 수행됨
        List<Post> savedPosts = postRepository.saveAll(allPosts);
        
        // PostTag 생성 및 저장
        List<PostTag> allPostTags = new ArrayList<>();
        for (Post post : savedPosts) {
            if (post.getTagIds() != null && !post.getTagIds().isEmpty()) {
                for (Long tagId : post.getTagIds()) {
                    allPostTags.add(PostTag.builder()
                            .postId(post.getId())
                            .tagId(tagId)
                            .build());
                }
            }
        }
        
        if (!allPostTags.isEmpty()) {
            postTagRepository.saveAll(allPostTags);
        }

        log.info("JPA Insert 완료: Post {}건, PostTag {}건", savedPosts.size(), allPostTags.size());
    }
}

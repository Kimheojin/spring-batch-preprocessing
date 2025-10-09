package HeoJin.SpringBatch.job.dummyDataJob.dummyDataWriter;

import HeoJin.SpringBatch.entity.dummyData.Post;
import HeoJin.SpringBatch.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDataWriter implements ItemStreamWriter<List<Post>> {
    private final PostRepository postRepository;


    @Override
    public void write(Chunk<? extends List<Post>> chunk) throws Exception {
        // chunk  단편화
        List<Post> allPosts = chunk.getItems().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        postRepository.saveAll(allPosts);
        log.info("{}개의 Post 저장 완료", allPosts.size());
    }
}

package HeoJin.SpringBatch.job.dummyDataJob.dummyDataWriter;

import HeoJin.SpringBatch.entity.dummyData.Post;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemStreamWriter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DummyDataWriter implements ItemStreamWriter<List<Post>> {

    private final JdbcTemplate jdbcTemplate;

    public DummyDataWriter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void write(Chunk<? extends List<Post>> chunk) throws Exception {
        // chunk 단편화 (List<List<Post>> -> List<Post>)
        List<Post> allPosts = chunk.getItems().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        if (allPosts.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO post (title, content, reg_date, status, category_id, member_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(sql,
                allPosts,
                allPosts.size(),
                (PreparedStatement ps, Post post) -> {
                    ps.setString(1, post.getTitle());
                    ps.setString(2, post.getContent());
                    ps.setTimestamp(3, Timestamp.valueOf(post.getRegDate()));
                    ps.setString(4, post.getStatus().name());
                    ps.setLong(5, post.getCategory().getId());
                    ps.setLong(6, post.getMember().getId());
                });

        log.info("JDBC Batch Insert 완료: {}건", allPosts.size());
    }
}

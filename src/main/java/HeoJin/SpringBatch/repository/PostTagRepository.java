package HeoJin.SpringBatch.repository;

import HeoJin.SpringBatch.entity.dummyData.tag.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
}

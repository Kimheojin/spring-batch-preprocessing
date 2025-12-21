package HeoJin.SpringBatch.repository;

import HeoJin.SpringBatch.entity.dummyData.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}

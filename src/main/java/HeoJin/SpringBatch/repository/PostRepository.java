package HeoJin.SpringBatch.repository;


import HeoJin.SpringBatch.entity.dummyData.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
}

package HeoJin.SpringBatch.repository;

import HeoJin.SpringBatch.entity.BatchError.BatchError;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatchErrorRepository extends JpaRepository<BatchError,Long> {
}

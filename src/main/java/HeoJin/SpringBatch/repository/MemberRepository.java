package HeoJin.SpringBatch.repository;


import HeoJin.SpringBatch.entity.dummyData.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    // role 이랑 같이 가져옴
    @EntityGraph(attributePaths = "role")
    Optional<Member> findByEmail(String email);
}

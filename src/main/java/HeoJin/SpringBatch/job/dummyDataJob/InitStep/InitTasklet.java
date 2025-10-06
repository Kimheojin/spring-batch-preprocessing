package HeoJin.SpringBatch.job.dummyDataJob.InitStep;

import HeoJin.SpringBatch.entity.dummyData.Category;
import HeoJin.SpringBatch.entity.dummyData.Member;
import HeoJin.SpringBatch.entity.dummyData.Role;
import HeoJin.SpringBatch.repository.CategoryRepository;
import HeoJin.SpringBatch.repository.MemberRepository;
import HeoJin.SpringBatch.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitTasklet implements Tasklet {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        final String INIT_EMAIL = "hurjin1109@naver.com";
        final String INIT_ROLE = "ADMIN";

        Optional<Member> testMember = memberRepository.findByEmail(INIT_EMAIL);

        if(testMember.isEmpty()){
            Role adminRole = roleRepository.findByRoleName(INIT_ROLE)
                    .orElseGet(() -> {
                        log.info("ADMIN 생성");
                        return roleRepository.save(Role.builder()
                                .roleName(INIT_ROLE)
                                .build());
                    });

            Member member = Member.builder()
                    .email(INIT_EMAIL)
                    .password("1234")
                    .memberName("허진")
                    .role(adminRole)
                    .build();

            memberRepository.save(member);
            log.info("새로운 member 저장 완료");
        }

        initCategory();

        return RepeatStatus.FINISHED;
    }

    private void initCategory() {
        long currentCount = categoryRepository.count();

        if(currentCount < 50) {
            log.info("카테고리 {}개 -> 50개로 확장", currentCount);
            for (int i = (int)currentCount; i < 50; i++) {
                categoryRepository.save(Category.builder()
                        .categoryName("카테고리" + (i + 1))
                        .priority((long) i)
                        .build());
            }
        }
    }
}

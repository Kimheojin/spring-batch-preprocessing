package HeoJin.SpringBatch.job.dummyDataJob.InitStep;

import HeoJin.SpringBatch.entity.dummyData.category.Category;
import HeoJin.SpringBatch.entity.dummyData.member.Member;
import HeoJin.SpringBatch.entity.dummyData.member.Role;
import HeoJin.SpringBatch.entity.dummyData.tag.Tag;
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
    private final HeoJin.SpringBatch.repository.TagRepository tagRepository;

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
        initTag();

        return RepeatStatus.FINISHED;
    }

    private void initCategory() {
        long currentCount = categoryRepository.count();
        final int TARGET_COUNT = 100;

        if(currentCount < TARGET_COUNT) {
            log.info("카테고리 {}개 -> {}개로 확장", currentCount, TARGET_COUNT);
            for (int i = (int)currentCount; i < TARGET_COUNT; i++) {
                categoryRepository.save(Category.builder()
                        .categoryName("카테고리" + (i + 1))
                        .priority((long) i)
                        .build());
            }
        }
    }

    private void initTag() {
        long currentCount = tagRepository.count();
        final int TARGET_COUNT = 50;

        if (currentCount < TARGET_COUNT) {
            log.info("태그 {}개 -> {}개로 확장", currentCount, TARGET_COUNT);
            for (int i = (int) currentCount; i < TARGET_COUNT; i++) {
                try {
                    tagRepository.save(Tag.builder()
                            .tagName("태그" + (i + 1))
                            .build());
                } catch (Exception e) {
                    log.warn("태그 저장 중 오류 발생 (중복 등): {}", e.getMessage());
                }
            }
        }
    }
}

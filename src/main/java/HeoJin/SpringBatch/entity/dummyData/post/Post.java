package HeoJin.SpringBatch.entity.dummyData.post;

import HeoJin.SpringBatch.entity.dummyData.member.Member;
import HeoJin.SpringBatch.entity.dummyData.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(catalog = "test-database")
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime regDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn( // 외래키
            name = "category_id"
    )
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.PRIVATE; // 비공개 디폴트

    @Transient
    @Setter
    private java.util.List<Long> tagIds;

    public void changeStatus(PostStatus status) {
        this.status = status;
    }


}
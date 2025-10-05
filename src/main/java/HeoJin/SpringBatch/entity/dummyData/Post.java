package HeoJin.SpringBatch.entity.dummyData;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
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

    public void updatePost(String title, String content, PostStatus postStatus){
        this.title = title;
        this.content = content;
        this.status = postStatus;
    }

    public void changeStatus(PostStatus status) {
        this.status = status;
    }


}
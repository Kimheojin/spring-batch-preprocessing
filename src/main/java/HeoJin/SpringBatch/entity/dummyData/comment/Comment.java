package HeoJin.SpringBatch.entity.dummyData.comment;

import HeoJin.SpringBatch.entity.dummyData.post.Post;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Table(catalog = "test-database")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;
    @Email
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommentStatus status = CommentStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "post_id",
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "FOREIGN KEY (post_id) REFERENCES post(post_id) ON DELETE CASCADE"
            ))
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id",
            foreignKey = @ForeignKey(
                    foreignKeyDefinition = "FOREIGN KEY (parent_id) REFERENCES comment(id) ON DELETE CASCADE"
            ))
    private Comment parent;

    @Column(updatable = false)
    private LocalDateTime regDate;



}

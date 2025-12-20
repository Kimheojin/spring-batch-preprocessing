package HeoJin.SpringBatch.entity.dummyData.tag;


import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "post_tag")
public class PostTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_tag_id")
    private Long id;
    @Column(name = "post_id")
    private Long postId;

    @Column(name = "tag_id")
    private Long tagId;

}

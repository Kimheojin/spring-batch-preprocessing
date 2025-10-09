package HeoJin.SpringBatch.entity.dummyData;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@AllArgsConstructor
@Table(catalog = "test-database")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;


    @Column(unique = true)
    private String categoryName;

    @Column(nullable = false)
    @Builder.Default
    private Long priority = 0L;

}

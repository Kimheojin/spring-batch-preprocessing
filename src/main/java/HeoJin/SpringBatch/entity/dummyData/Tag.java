package HeoJin.SpringBatch.entity.dummyData;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, name = "tag_name")
    private String tagName;


    @PrePersist
    @PreUpdate
    public void toLowerCase(){
        // 자동으로 소문자로
        if (this.tagName != null) {
            this.tagName = this.tagName.toLowerCase();
        }
    }

}

package cersei.profile.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity(name = "user_profile")
@DynamicUpdate
public class UserProfileModel {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 32)
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

//    @Version
//    @Column(nullable = false)
//    private int version;
}

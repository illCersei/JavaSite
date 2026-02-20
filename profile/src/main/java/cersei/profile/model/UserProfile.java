package cersei.profile.model;

import jakarta.persistence.*;
import cersei.auth.model.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "user_profile")
public class UserProfile {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 32)
    private String nickname;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Version
    @Column(nullable = false)
    private int version;
}

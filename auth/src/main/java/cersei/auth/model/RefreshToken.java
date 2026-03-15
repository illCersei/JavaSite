package cersei.auth.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Data
public class RefreshToken {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    @Column(unique = true)
    private String token;

    private Instant expiresAt;
}
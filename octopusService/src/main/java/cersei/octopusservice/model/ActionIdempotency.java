package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "action_idempotency",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_action_idempotency",
                columnNames = {"user_id", "action_type", "idempotency_key"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class ActionIdempotency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "action_type", nullable = false, length = 64)
    private String actionType;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Lob
    @Column(name = "response_json")
    private String responseJson;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}


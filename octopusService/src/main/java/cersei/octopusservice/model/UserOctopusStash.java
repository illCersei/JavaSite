package cersei.octopusservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_octopus_stash")
@IdClass(UserOctopusStashId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserOctopusStash {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Id
    @Column(name = "octopus_id", nullable = false)
    private int octopusId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserOctopusStash(UUID userId, int octopusId, int quantity, Instant updatedAt) {
        this.userId = userId;
        this.octopusId = octopusId;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }
}

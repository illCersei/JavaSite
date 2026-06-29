package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_battle_team_slot",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_battle_team_slot", columnNames = {"user_id", "slot_index"}),
                @UniqueConstraint(name = "uq_user_battle_team_octopus", columnNames = {"user_id", "user_octopus_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class UserBattleTeamSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_octopus_id", nullable = false)
    private UserOctopus userOctopus;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}

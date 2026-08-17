package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_skill_unlock",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_skill_unlock",
                columnNames = {"user_id", "skill_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class UserSkillUnlock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private OctopusSkill skill;

    @Column(name = "unlocked_at", nullable = false)
    private Instant unlockedAt = Instant.now();
}

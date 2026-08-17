package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_octopus_skill_slot",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_octopus_skill_slot",
                columnNames = {"user_octopus_id", "slot_index"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class UserOctopusSkillSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_octopus_id", nullable = false)
    private UserOctopus userOctopus;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @ManyToOne
    @JoinColumn(name = "skill_id")
    private OctopusSkill skill;
}


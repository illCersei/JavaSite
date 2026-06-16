package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.CombatRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@Table(name = "user_octopus")
public class UserOctopus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false)
    private Integer level = 1;

    @Column(name = "current_tier", nullable = false)
    private Integer currentTier = 1;

    @Column(nullable = false)
    private Integer stars = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private CombatRole role = CombatRole.BRUISER;

    @Column(nullable = false)
    private Integer exp = 0;

    @Column(name = "attack_stat", nullable = false)
    private Integer currentAttackStat;

    @Column(name = "magic_power_stat", nullable = false)
    private Integer currentMagicPowerStat;

    @Column(name = "armor_stat", nullable = false)
    private Integer currentArmorStat;

    @Column(name = "magic_resist_stat", nullable = false)
    private Integer currentMagicResistStat;

    @Column(name = "speed_stat", nullable = false)
    private Integer currentSpeedStat;

    @Column(name = "free_skill_points", nullable = false)
    private Integer currentFreeSkillPoints;

    @ManyToOne(optional = false)
    @JoinColumn(name = "base_octopus", nullable = false)
    private Octopus octopus;

    /**
     * Skills this instance may pick into {@link UserOctopusSkillSlot} (spell pool unlocked for this octopus).
     */
    @ManyToMany
    @JoinTable(
            name = "user_octopus_open_skill",
            joinColumns = @JoinColumn(name = "user_octopus_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<OctopusSkill> openSkills = new HashSet<>();
}

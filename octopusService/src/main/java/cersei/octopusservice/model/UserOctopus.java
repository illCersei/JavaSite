package cersei.octopusservice.model;

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

    @Column(nullable = false)
    private Integer level = 1;

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

    @ManyToOne(optional = false)
    @JoinColumn(name = "base_octopus", nullable = false)
    private Octopus octopus;

    @ManyToMany
    @JoinTable(
            name = "user_octopus_perk",
            joinColumns = @JoinColumn(name = "user_octopus_id"),
            inverseJoinColumns = @JoinColumn(name = "perk_id")
    )
    private Set<OctopusSkill> perks = new HashSet<>();
}

package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.ElementType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "octopus")
@Getter
@Setter
@NoArgsConstructor
public class Octopus {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "element_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ElementType elementType;

    @Column(name = "tier", nullable = false)
    private Integer tier;

    @Column(name = "attack_stat", nullable = false)
    private Integer attackStat;

    @Column(name = "magic_power_stat", nullable = false)
    private Integer magicPowerStat;

    @Column(name = "armor_stat", nullable = false)
    private Integer armorStat;

    @Column(name = "magic_resist_stat", nullable = false)
    private Integer magicResistStat;

    @Column(name = "speed_stat", nullable = false)
    private Integer speedStat;

    @Column(name = "free_skill_points", nullable = false)
    private Integer freeSkillPoints;
}

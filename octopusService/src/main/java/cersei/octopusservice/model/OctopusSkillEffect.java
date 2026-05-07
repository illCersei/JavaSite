package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.ElementType;
import cersei.octopusservice.model.utils.ScalingStat;
import cersei.octopusservice.model.utils.SkillEffectType;
import cersei.octopusservice.model.utils.StackingRule;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "octopus_skill_effect")
@Getter
@Setter
@NoArgsConstructor
public class OctopusSkillEffect {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private OctopusSkill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "effect_type", nullable = false, length = 32)
    private SkillEffectType effectType;

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false, length = 32)
    private ElementType elementType;

    /**
     * Flat base value. For DAMAGE/DOT this is per-hit or per-tick value.
     */
    @Column(name = "base_value", nullable = false)
    private Integer baseValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "scaling_stat", length = 32)
    private ScalingStat scalingStat;

    @Column(name = "scaling_ratio_bps")
    private Integer scalingRatioBps;

    // for dot, null for non-dot
    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "tick_ms")
    private Integer tickMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "stacking_rule", length = 32)
    private StackingRule stackingRule;
}


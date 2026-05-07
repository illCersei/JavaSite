package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.ElementType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "octopus_skill")
@Getter
@Setter
@NoArgsConstructor
public class OctopusSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false, length = 32)
    private ElementType elementType = ElementType.PHYSICAL;

    @Column(name = "cooldown_ms", nullable = false)
    private Integer cooldownMs = 0;

    @Column(name = "mana_cost", nullable = false)
    private Integer manaCost = 0;

    @OneToMany(mappedBy = "skill", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OctopusSkillEffect> effects = new ArrayList<>();
}

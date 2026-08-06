package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.ItemSlot;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "item")
@Getter
@Setter
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemSlot slot;

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

    @Column(name = "crit_chance", nullable = false)
    private Integer critChance;

    @Column(name = "crit_damage", nullable = false)
    private Integer critDamage;

    @Column(name = "accuracy", nullable = false)
    private Integer accuracy;

    @Column(name = "evasion", nullable = false)
    private Integer evasion;

    @Column(name = "tenacity", nullable = false)
    private Integer tenacity;

    @Column(name = "status_power", nullable = false)
    private Integer statusPower;
}
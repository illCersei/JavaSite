package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.DungeonRoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dungeon_room_template",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dungeon_room",
                columnNames = {"dungeon_template_id", "room_index"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DungeonRoomTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dungeon_template_id", nullable = false)
    private DungeonTemplate dungeonTemplate;

    @Column(name = "room_index", nullable = false)
    private Integer roomIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 32)
    private DungeonRoomType roomType;

    @Column(name = "enemy_template_id", length = 64)
    private String enemyTemplateId;

    @ManyToOne
    @JoinColumn(name = "loot_item_id")
    private Item lootItem;

    @Column(name = "loot_quantity", nullable = false)
    private Integer lootQuantity = 0;

    @Column(name = "loot_coins_minor", nullable = false)
    private Long lootCoinsMinor = 0L;
}
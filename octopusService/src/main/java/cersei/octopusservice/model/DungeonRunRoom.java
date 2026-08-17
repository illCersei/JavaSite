package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.DungeonRoomStatus;
import cersei.octopusservice.model.utils.DungeonRoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dungeon_run_room",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dungeon_run_room_position",
                columnNames = {"dungeon_run_id", "layer_index", "slot_index"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DungeonRunRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dungeon_run_id", nullable = false)
    private DungeonRun dungeonRun;

    @Column(name = "layer_index", nullable = false)
    private Integer layerIndex;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 32)
    private DungeonRoomType roomType;

    @Column(name = "enemy_template_id", length = 64)
    private String enemyTemplateId;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_status", nullable = false, length = 32)
    private DungeonRoomStatus roomStatus = DungeonRoomStatus.LOCKED;

    @ManyToOne
    @JoinColumn(name = "loot_item_id")
    private Item lootItem;

    @Column(name = "loot_quantity", nullable = false)
    private Integer lootQuantity = 0;

    @Column(name = "loot_coins_minor", nullable = false)
    private Long lootCoinsMinor = 0L;
}
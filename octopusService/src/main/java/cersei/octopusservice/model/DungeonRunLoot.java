package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "dungeon_run_loot")
@Getter
@Setter
@NoArgsConstructor
public class DungeonRunLoot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dungeon_run_id", nullable = false)
    private DungeonRun dungeonRun;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(name = "coins_minor", nullable = false)
    private Long coinsMinor = 0L;
}
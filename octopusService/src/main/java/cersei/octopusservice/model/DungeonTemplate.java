package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "dungeon_template")
@Getter
@Setter
@NoArgsConstructor
public class DungeonTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer tier;

    @Column(name = "entry_cost_minor", nullable = false)
    private Long entryCostMinor;

    @Column(name = "room_count", nullable = false)
    private Integer roomCount;

    @Column(name = "depth_layers", nullable = false)
    private Integer depthLayers = 3;
}
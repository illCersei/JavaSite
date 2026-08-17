package cersei.octopusservice.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "dungeon_run_room_link",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_dungeon_run_room_link",
                columnNames = {"from_room_id", "to_room_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
public class DungeonRunRoomLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dungeon_run_id", nullable = false, columnDefinition = "uuid")
    private java.util.UUID dungeonRunId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_room_id", nullable = false)
    private DungeonRunRoom fromRoom;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_room_id", nullable = false)
    private DungeonRunRoom toRoom;
}
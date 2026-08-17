package cersei.octopusservice.model;

import cersei.octopusservice.model.utils.DungeonRunStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "dungeon_run")
@Getter
@Setter
@NoArgsConstructor
public class DungeonRun {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "dungeon_template_id", nullable = false)
    private DungeonTemplate dungeonTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private DungeonRunStatus status = DungeonRunStatus.ACTIVE;

    @Column(name = "current_room_index", nullable = false)
    private Integer currentRoomIndex = 0;

    @Column(name = "current_room_id")
    private Long currentRoomId;

    @Column(name = "rng_seed", nullable = false)
    private Long rngSeed;

    @Column(name = "current_fight_id", length = 64)
    private String currentFightId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
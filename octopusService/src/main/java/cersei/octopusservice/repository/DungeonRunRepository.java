package cersei.octopusservice.repository;

import cersei.octopusservice.model.DungeonRun;
import cersei.octopusservice.model.utils.DungeonRunStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DungeonRunRepository extends JpaRepository<DungeonRun, UUID> {

    Optional<DungeonRun> findByIdAndUserId(UUID id, UUID userId);

    List<DungeonRun> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, DungeonRunStatus status);
}
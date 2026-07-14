package cersei.octopusservice.repository;

import cersei.octopusservice.model.DungeonRunLoot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DungeonRunLootRepository extends JpaRepository<DungeonRunLoot, Long> {

    List<DungeonRunLoot> findByDungeonRun_Id(UUID dungeonRunId);
}
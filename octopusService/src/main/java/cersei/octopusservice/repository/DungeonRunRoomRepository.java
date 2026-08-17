package cersei.octopusservice.repository;

import cersei.octopusservice.model.DungeonRunRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DungeonRunRoomRepository extends JpaRepository<DungeonRunRoom, Long> {

    List<DungeonRunRoom> findByDungeonRun_IdOrderByLayerIndexAscSlotIndexAsc(UUID dungeonRunId);

    Optional<DungeonRunRoom> findByIdAndDungeonRun_Id(Long id, UUID dungeonRunId);
}
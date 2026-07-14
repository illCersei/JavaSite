package cersei.octopusservice.repository;

import cersei.octopusservice.model.DungeonRunRoomLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DungeonRunRoomLinkRepository extends JpaRepository<DungeonRunRoomLink, Long> {

    List<DungeonRunRoomLink> findByDungeonRunId(UUID dungeonRunId);
}
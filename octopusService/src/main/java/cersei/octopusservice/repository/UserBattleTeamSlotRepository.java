package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserBattleTeamSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserBattleTeamSlotRepository extends JpaRepository<UserBattleTeamSlot, Long> {

    @EntityGraph(attributePaths = {"userOctopus", "userOctopus.octopus"})
    List<UserBattleTeamSlot> findByUserIdOrderBySlotIndexAsc(UUID userId);

    void deleteByUserId(UUID userId);
}

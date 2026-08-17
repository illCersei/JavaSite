package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserBattleTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserBattleTeamRepository extends JpaRepository<UserBattleTeam, UUID> {
}
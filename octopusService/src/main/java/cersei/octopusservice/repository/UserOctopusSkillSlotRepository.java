package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserOctopusSkillSlot;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOctopusSkillSlotRepository extends JpaRepository<UserOctopusSkillSlot, Integer> {

    @EntityGraph(attributePaths = "skill")
    List<UserOctopusSkillSlot> findByUserOctopus_IdOrderBySlotIndexAsc(Integer userOctopusId);
}
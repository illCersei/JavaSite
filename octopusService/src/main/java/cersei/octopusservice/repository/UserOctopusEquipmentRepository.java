package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserOctopusEquipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserOctopusEquipmentRepository extends JpaRepository<UserOctopusEquipment, Integer> {

    @EntityGraph(attributePaths = "item")
    List<UserOctopusEquipment> findByUserOctopus_IdOrderByIdAsc(Integer userOctopusId);
}
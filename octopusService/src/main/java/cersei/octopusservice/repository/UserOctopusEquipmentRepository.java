package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserOctopusEquipment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import cersei.octopusservice.model.utils.ItemSlot;

import java.util.List;
import java.util.Optional;

public interface UserOctopusEquipmentRepository extends JpaRepository<UserOctopusEquipment, Integer> {

    @EntityGraph(attributePaths = "item")
    List<UserOctopusEquipment> findByUserOctopus_IdOrderByIdAsc(Integer userOctopusId);

    @EntityGraph(attributePaths = "item")
    Optional<UserOctopusEquipment> findByUserOctopus_IdAndSlot(Integer userOctopusId, ItemSlot slot);
}
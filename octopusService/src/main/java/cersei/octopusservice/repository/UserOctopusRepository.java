package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserOctopus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOctopusRepository extends JpaRepository<UserOctopus, Integer> {

    @EntityGraph(attributePaths = {
            "octopus",
            "openSkills"
    })
    Optional<UserOctopus> findByIdAndUserId(Integer id, UUID userId);

    @EntityGraph(attributePaths = {
            "octopus",
            "openSkills"
    })
    List<UserOctopus> findByUserIdOrderByIdAsc(UUID userId);

    long countByUserIdAndOctopus_Id(UUID userId, Integer octopusId);
}
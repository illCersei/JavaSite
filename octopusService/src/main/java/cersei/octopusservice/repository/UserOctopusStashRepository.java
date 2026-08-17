package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserOctopusStash;
import cersei.octopusservice.model.UserOctopusStashId;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserOctopusStashRepository extends JpaRepository<UserOctopusStash, UserOctopusStashId> {

    List<UserOctopusStash> findByUserIdOrderByOctopusIdAsc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserOctopusStash> findByUserIdAndOctopusId(UUID userId, int octopusId);
}

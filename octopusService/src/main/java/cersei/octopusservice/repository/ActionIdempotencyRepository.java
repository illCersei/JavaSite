package cersei.octopusservice.repository;

import cersei.octopusservice.model.ActionIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ActionIdempotencyRepository extends JpaRepository<ActionIdempotency, Long> {
    Optional<ActionIdempotency> findByUserIdAndActionTypeAndIdempotencyKey(UUID userId, String actionType, String idempotencyKey);
}


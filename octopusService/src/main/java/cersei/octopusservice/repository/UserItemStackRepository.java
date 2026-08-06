package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserItemStack;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserItemStackRepository extends JpaRepository<UserItemStack, Long> {

    @EntityGraph(attributePaths = "item")
    List<UserItemStack> findByUserIdOrderByItem_IdAsc(UUID userId);

    // Locked (unlike the read-only listing above) because every caller of this method
    // does a read-modify-write on quantity (addItems/consumeItems) - without the lock,
    // two concurrent calls for the same stack can both read the same quantity and one
    // silently loses its update (see UserOctopusStashRepository for the same pattern).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = "item")
    Optional<UserItemStack> findByUserIdAndItem_Id(UUID userId, Integer itemId);
}

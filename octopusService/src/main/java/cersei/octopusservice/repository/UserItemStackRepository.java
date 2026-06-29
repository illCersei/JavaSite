package cersei.octopusservice.repository;

import cersei.octopusservice.model.UserItemStack;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserItemStackRepository extends JpaRepository<UserItemStack, Long> {

    @EntityGraph(attributePaths = "item")
    List<UserItemStack> findByUserIdOrderByItem_IdAsc(UUID userId);

    @EntityGraph(attributePaths = "item")
    Optional<UserItemStack> findByUserIdAndItem_Id(UUID userId, Integer itemId);
}

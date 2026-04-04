package cersei.pokemonservice.repository;

import cersei.pokemonservice.model.UserPokemonStash;
import cersei.pokemonservice.model.UserPokemonStashId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPokemonStashRepository extends JpaRepository<UserPokemonStash, UserPokemonStashId> {

    List<UserPokemonStash> findByUserIdOrderByPokemonIdAsc(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserPokemonStash> findByUserIdAndPokemonId(UUID userId, int pokemonId);
}

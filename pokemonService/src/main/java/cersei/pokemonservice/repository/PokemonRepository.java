package cersei.pokemonservice.repository;

import cersei.pokemonservice.model.Pokemon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PokemonRepository extends JpaRepository<Pokemon, Integer> {}

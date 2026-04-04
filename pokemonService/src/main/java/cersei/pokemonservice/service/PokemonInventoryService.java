package cersei.pokemonservice.service;

import cersei.pokemonservice.dto.InventoryLineDto;
import cersei.pokemonservice.dto.PokemonSummaryDto;
import cersei.pokemonservice.model.UserPokemonStash;
import cersei.pokemonservice.repository.UserPokemonStashRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PokemonInventoryService {

    private final UserPokemonStashRepository userPokemonStashRepository;
    private final PokemonCatalogService pokemonCatalogService;

    @Transactional
    public int addOne(UUID userId, int pokemonId) {
        UserPokemonStash row = userPokemonStashRepository
                .findByUserIdAndPokemonId(userId, pokemonId)
                .orElseGet(() -> new UserPokemonStash(userId, pokemonId, 0, Instant.now()));
        row.setQuantity(row.getQuantity() + 1);
        row.setUpdatedAt(Instant.now());
        userPokemonStashRepository.save(row);
        return row.getQuantity();
    }

    @Transactional(readOnly = true)
    public List<InventoryLineDto> listWithDetails(UUID userId) {
        return userPokemonStashRepository.findByUserIdOrderByPokemonIdAsc(userId).stream()
                .map(this::toLine)
                .toList();
    }

    private InventoryLineDto toLine(UserPokemonStash stash) {
        PokemonSummaryDto base = pokemonCatalogService.getById(stash.getPokemonId());
        PokemonSummaryDto withQty = new PokemonSummaryDto(
                base.id(), base.name(), base.imageUrl(), base.weight(), stash.getQuantity());
        return new InventoryLineDto(withQty);
    }
}

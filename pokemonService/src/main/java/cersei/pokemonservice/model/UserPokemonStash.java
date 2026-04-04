package cersei.pokemonservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_pokemon_stash")
@IdClass(UserPokemonStashId.class)
@Getter
@Setter
@NoArgsConstructor
public class UserPokemonStash {

    @Id
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid")
    private UUID userId;

    @Id
    @Column(name = "pokemon_id", nullable = false)
    private int pokemonId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public UserPokemonStash(UUID userId, int pokemonId, int quantity, Instant updatedAt) {
        this.userId = userId;
        this.pokemonId = pokemonId;
        this.quantity = quantity;
        this.updatedAt = updatedAt;
    }
}

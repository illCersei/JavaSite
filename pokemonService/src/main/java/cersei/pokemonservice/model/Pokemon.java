package cersei.pokemonservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pokemon")
@Getter
@Setter
@NoArgsConstructor
public class Pokemon {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "name", nullable = false)
    private String name;

    //Ордер походу зарезервирован в SQL, поэтому пришлось экранировать
    @Column(name = "\"order\"", nullable = false)
    private Integer sortOrder;

    @Column(name = "height", nullable = false)
    private Integer height;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    @Column(name = "base_experience")
    private Integer baseExperience;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;

    @Column(name = "pokemon_species_id")
    private Integer pokemonSpeciesId;
}

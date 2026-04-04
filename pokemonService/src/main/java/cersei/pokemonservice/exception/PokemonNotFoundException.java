package cersei.pokemonservice.exception;

public class PokemonNotFoundException extends RuntimeException {

    public PokemonNotFoundException(int id) {
        super("Pokemon not found: " + id);
    }
}

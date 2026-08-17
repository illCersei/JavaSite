package cersei.octopusservice.exception;

public class OctopusNotFoundException extends RuntimeException {

    public OctopusNotFoundException(int id) {
        super("Octopus not found: " + id);
    }
}

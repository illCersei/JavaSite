package cersei.octopusservice.exception;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException(int id) {
        super("Item not found: " + id);
    }
}

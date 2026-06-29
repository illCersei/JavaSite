package cersei.octopusservice.exception;

public class InsufficientItemQuantityException extends RuntimeException {

    public InsufficientItemQuantityException(int itemId, int requested, int available) {
        super("Insufficient quantity for item " + itemId + ": requested " + requested + ", available " + available);
    }
}

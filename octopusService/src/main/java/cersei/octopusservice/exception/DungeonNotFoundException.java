package cersei.octopusservice.exception;

import java.util.UUID;

public class DungeonNotFoundException extends RuntimeException {

    public DungeonNotFoundException(UUID runId) {
        super("Dungeon run not found: " + runId);
    }
}
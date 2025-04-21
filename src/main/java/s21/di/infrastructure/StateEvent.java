package s21.di.infrastructure;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class StateEvent extends ApplicationEvent {
    private final UUID gameIdentifier;

    public StateEvent(Object source, UUID gameId) {
        super(source);
        this.gameIdentifier = gameId;
    }

    public UUID getIdentifier() {
        return gameIdentifier;
    }
}

package s21.web.model;

import java.util.UUID;

public class GameDTO {
    private UUID uuid;
    private UUID ownerId;
    private UUID opponentId;
    private String creationDate;
    private GameFieldDTO field;

    public GameDTO() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getOpponentId() {
        return opponentId;
    }

    public void setOpponentId(UUID opponentId) {
        this.opponentId = opponentId;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public GameFieldDTO getField() {
        return field;
    }

    public void setField(GameFieldDTO field) {
        this.field = field;
    }

}

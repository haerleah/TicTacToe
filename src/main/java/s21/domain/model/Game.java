package s21.domain.model;

import java.util.Date;
import java.util.UUID;

public class Game {
    private UUID uuid;
    private GameField field;
    private boolean isSinglePlayer;
    private Date creationDate;
    private UUID ownerID;
    private UUID opponentID;
    private int ownerMark;
    private int opponentMark;
    private GameStatus currentGameStatus;

    public Game() {
        this.uuid = UUID.randomUUID();
        this.field = new GameField();
    }

    public Game(UUID ownerID, String mark, boolean isSinglePlayer) {
        this.uuid = UUID.randomUUID();
        this.field = new GameField();
        this.ownerID = ownerID;
        this.isSinglePlayer = isSinglePlayer;
        this.ownerMark = mark.equals("zero") ? 1 : -1;
        this.opponentMark = ownerMark == 1 ? -1 : 1;
        this.opponentID = null;
        this.creationDate = new Date();
        currentGameStatus = isSinglePlayer ? GameStatus.OWNER_TURN : GameStatus.WAITING_FOR_PLAYERS;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public GameField getField() {
        return field;
    }

    public void setField(GameField field) {
        this.field = field;
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }

    public void setSinglePlayer(boolean singlePlayer) {
        isSinglePlayer = singlePlayer;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public UUID getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(UUID ownerID) {
        this.ownerID = ownerID;
    }

    public void setOpponentID(UUID opponentID) {
        this.opponentID = opponentID;
    }

    public UUID getOpponentID() {
        return opponentID;
    }

    public int getOwnerMark() {
        return ownerMark;
    }

    public void setOwnerMark(int ownerMark) {
        this.ownerMark = ownerMark;
    }

    public int getOpponentMark() {
        return opponentMark;
    }

    public void setOpponentMark(int opponentMark) {
        this.opponentMark = opponentMark;
    }

    public GameStatus getCurrentGameStatus() {
        return currentGameStatus;
    }

    public void setCurrentGameStatus(GameStatus currentGameStatus) {
        this.currentGameStatus = currentGameStatus;
    }
}

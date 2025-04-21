package s21.datasource.model;

import jakarta.persistence.*;
import s21.domain.model.GameStatus;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "games")
public class GameEntity {
    @Id
    @Column(name = "uuid", updatable = false, nullable = false, unique = true)
    private UUID uuid;

    @Column(nullable = false)
    private boolean singlePlayer;

    @Column(name = "owner_mark", nullable = false)
    private int ownerMark;

    @Column(name = "opponent_mark", nullable = false)
    private int opponentMark;

    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "owner_id",
            referencedColumnName = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_game_owner")
    )
    private UserEntity owner;

    @ManyToOne
    @JoinColumn(
            name = "opponent_id",
            referencedColumnName = "user_id",
            foreignKey = @ForeignKey(name = "fk_game_opponent")
    )
    private UserEntity opponent;

    @Embedded
    private GameFieldEntity field;

    @Column(name = "game_status", nullable = false)
    @Enumerated(EnumType.STRING)
    GameStatus status;

    public GameEntity() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isSinglePlayer() {
        return singlePlayer;
    }

    public void setSinglePlayer(boolean singlePlayer) {
        this.singlePlayer = singlePlayer;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public UserEntity getOpponent() {
        return opponent;
    }

    public void setOpponent(UserEntity opponent) {
        this.opponent = opponent;
    }

    public GameFieldEntity getField() {
        return field;
    }

    public void setField(GameFieldEntity field) {
        this.field = field;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }
}

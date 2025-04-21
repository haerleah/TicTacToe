package s21.datasource.mapper;

import s21.datasource.model.*;
import s21.domain.model.Game;
import s21.domain.model.GameField;
import s21.domain.model.StatisticModel;
import s21.domain.model.User;
import s21.security.model.RefreshToken;

import java.util.Arrays;
import java.util.stream.IntStream;

public class DataSourceMapper {
    public static GameEntity gameToEntity(Game game) {
        GameEntity gameEntity = new GameEntity();
        gameEntity.setUuid(game.getUuid());
        gameEntity.setField(fieldToEntity(game.getField()));
        gameEntity.setSinglePlayer(game.isSinglePlayer());
        gameEntity.setOwnerMark(game.getOwnerMark());
        gameEntity.setOpponentMark(game.getOpponentMark());
        gameEntity.setStatus(game.getCurrentGameStatus());
        gameEntity.setCreationDate(game.getCreationDate());

        UserEntity owner = new UserEntity();
        owner.setUuid(game.getOwnerID());
        gameEntity.setOwner(owner);

        if (game.getOpponentID() != null) {
            UserEntity opponent = new UserEntity();
            opponent.setUuid(game.getOpponentID());
            gameEntity.setOpponent(opponent);
        }

        return gameEntity;
    }

    public static GameFieldEntity fieldToEntity(GameField field) {
        StringBuilder builder = new StringBuilder();
        Arrays.stream(field.getField()).flatMapToInt(Arrays::stream).forEach(value -> {
            builder.append(value);
            builder.append(" ");
        });
        return new GameFieldEntity(builder.toString());
    }

    public static UserEntity userToEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(user.getUuid());
        userEntity.setLogin(user.getLogin());
        userEntity.setPassword(user.getPassword());
        userEntity.setRoles(user.getRoles());
        return userEntity;
    }

    public static Game entityToGame(GameEntity gameEntity) {
        Game gameInstance = new Game();
        gameInstance.setUuid(gameEntity.getUuid());
        gameInstance.setField(entityToField(gameEntity.getField()));
        gameInstance.setSinglePlayer(gameEntity.isSinglePlayer());
        gameInstance.setOwnerID(gameEntity.getOwner().getUuid());
        gameInstance.setOwnerMark(gameEntity.getOwnerMark());
        gameInstance.setOpponentMark(gameEntity.getOpponentMark());
        gameInstance.setCurrentGameStatus(gameEntity.getStatus());
        gameInstance.setCreationDate(gameEntity.getCreationDate());

        if (gameEntity.getOpponent() != null) {
            gameInstance.setOpponentID(gameEntity.getOpponent().getUuid());
        }
        return gameInstance;
    }

    public static GameField entityToField(GameFieldEntity gameFieldEntity) {
        GameField gameFieldInstance = new GameField();

        String[] tokens = gameFieldEntity.getField().split("\\s+");

        gameFieldInstance.setField(IntStream.range(0, 3)
                .mapToObj(row ->
                        IntStream.range(0, 3)
                                .map(col -> Integer.parseInt(tokens[row * 3 + col]))
                                .toArray()
                )
                .toArray(int[][]::new));
        return gameFieldInstance;
    }

    public static User entityToUser(UserEntity userEntity) {
        User user = new User();
        user.setUuid(userEntity.getUuid());
        user.setLogin(userEntity.getLogin());
        user.setPassword(userEntity.getPassword());
        user.setRoles(userEntity.getRoles());
        return user;
    }

    public static RefreshTokenEntity tokenToEntity(RefreshToken refreshToken) {
        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setToken(refreshToken.getToken());
        UserEntity userEntity = new UserEntity();
        userEntity.setUuid(refreshToken.getTokenOwnerUuid());
        refreshTokenEntity.setTokenOwner(userEntity);
        return refreshTokenEntity;
    }

    public static StatisticModel entityToStatistic(StatisticModelEntity statisticModel) {
        StatisticModel statisticModelInstance = new StatisticModel();
        statisticModelInstance.setUserId(statisticModel.getUserId());
        statisticModelInstance.setWinPercentage(statisticModel.getWinPercentage());
        return statisticModelInstance;
    }
}

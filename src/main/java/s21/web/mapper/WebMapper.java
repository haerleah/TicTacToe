package s21.web.mapper;

import s21.domain.model.Game;
import s21.domain.model.GameField;
import s21.domain.model.StatisticModel;
import s21.domain.model.User;
import s21.web.model.GameDTO;
import s21.web.model.GameFieldDTO;
import s21.web.model.StatisticModelDTO;
import s21.web.model.UserDTO;

import java.text.SimpleDateFormat;
import java.util.Arrays;

public class WebMapper {
    public static GameDTO gameToDTO(Game game) {
        GameDTO gameDTO = new GameDTO();
        gameDTO.setUuid(game.getUuid());
        gameDTO.setField(fieldToDTO(game.getField()));
        gameDTO.setOwnerId(game.getOwnerID());
        gameDTO.setOpponentId(game.getOpponentID());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm");
        gameDTO.setCreationDate(dateFormat.format(game.getCreationDate()));
        return gameDTO;
    }

    public static GameFieldDTO fieldToDTO(GameField field) {
        int[][] newField = Arrays.stream(field.getField())
                .map(row -> Arrays.copyOf(row, row.length))
                .toArray(int[][]::new);
        return new GameFieldDTO(newField);
    }

    public static UserDTO userToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUuid(user.getUuid());
        userDTO.setLogin(user.getLogin());
        return userDTO;
    }

    public static StatisticModelDTO statisticToDTO(StatisticModel statistic) {
        StatisticModelDTO statisticModelDTO = new StatisticModelDTO();
        statisticModelDTO.setUserId(statistic.getUserId());
        statisticModelDTO.setWinPercentage(statistic.getWinPercentage());
        return statisticModelDTO;
    }
}

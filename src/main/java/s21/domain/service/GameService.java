package s21.domain.service;

import s21.domain.model.Game;
import s21.domain.model.StatisticModel;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.TooManyListenersException;
import java.util.UUID;

public interface GameService {
    Game createNewGame(UUID ownerId, String mark, boolean isSinglePlayer);

    Game getGame(UUID gameId) throws NoSuchElementException;

    Collection<Game> getAllGames();

    Collection<Game> getAvailableMultiplayerGamesFor(UUID ownerId);

    Collection<Game> getCompletedGames(UUID ownerId);

    Collection<Game> getUnfinishedGames(UUID userId);

    Collection<StatisticModel> getBestPlayerStatistics(int numberOfPlayers);

    void nextStep(UUID gameId) throws NoSuchElementException;

    void joinUserToGame(UUID gameId, UUID participantId) throws NoSuchElementException, TooManyListenersException;

    boolean validateTurn(int i, int j, UUID gameId, UUID playerId) throws NoSuchElementException;

    boolean hasOnlineGame(UUID userId);

    void makeTurn(UUID gameId, UUID playerId, int i, int j) throws NoSuchElementException;
}

package s21.domain.service;

import s21.datasource.mapper.DataSourceMapper;
import s21.datasource.model.GameEntity;
import s21.datasource.repository.GameRepository;
import s21.di.infrastructure.EventNotifier;
import s21.di.infrastructure.StateEvent;
import s21.domain.model.Game;
import s21.domain.model.GameField;
import s21.domain.model.GameStatus;
import s21.domain.model.StatisticModel;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GameServiceImplementation implements GameService {
    private final GameRepository gameRepository;
    private final EventNotifier eventNotifier;

    public GameServiceImplementation(GameRepository gameRepository, EventNotifier eventNotifier) {
        this.gameRepository = gameRepository;
        this.eventNotifier = eventNotifier;
    }

    @Override
    public Game createNewGame(UUID ownerId, String mark, boolean isSinglePlayer) {
        Game newGame = new Game(ownerId, mark, isSinglePlayer);
        gameRepository.save(DataSourceMapper.gameToEntity(newGame));
        eventNotifier.createBlockingQueueChunk(newGame.getUuid(), ownerId);
        return newGame;
    }

    @Override
    public Game getGame(UUID gameId) throws NoSuchElementException {
        return DataSourceMapper.entityToGame(gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("No such element with given UUID")));
    }

    @Override
    public Collection<Game> getAllGames() {
        return StreamSupport.stream(gameRepository.findAll().spliterator(), false)
                .map(DataSourceMapper::entityToGame).toList();
    }

    @Override
    public void nextStep(UUID gameId) throws NoSuchElementException {
        Game currentGame = getGame(gameId);
        if (Stream.of(GameStatus.OWNER_WIN, GameStatus.OPPONENT_WIN, GameStatus.DRAW, GameStatus.WAITING_FOR_PLAYERS)
                .noneMatch(currentGame.getCurrentGameStatus()::equals)) {
            if (currentGame.isSinglePlayer()) {
                computerStep(currentGame);
            } else {
                currentGame.setCurrentGameStatus(currentGame.getCurrentGameStatus() == GameStatus.OWNER_TURN
                        ? GameStatus.OPPONENT_TURN : GameStatus.OWNER_TURN);
            }
            gameRepository.save(DataSourceMapper.gameToEntity(currentGame));
            eventNotifier.publishStateEvent(new StateEvent(this, gameId));
        }
    }

    @Override
    public void joinUserToGame(UUID gameId, UUID participantId) throws NoSuchElementException, TooManyListenersException {
        Game currentGame = getGame(gameId);
        if (currentGame.getOpponentID() == null) {
            currentGame.setOpponentID(participantId);
            currentGame.setCurrentGameStatus(GameStatus.OWNER_TURN);
            gameRepository.save(DataSourceMapper.gameToEntity(currentGame));
            eventNotifier.createBlockingQueueChunk(currentGame.getUuid(), participantId);
            eventNotifier.publishStateEvent(new StateEvent(this, gameId));
        } else {
            throw new TooManyListenersException("This game already has an opponent");
        }
    }

    @Override
    public boolean validateTurn(int i, int j, UUID gameId, UUID playerId) throws NoSuchElementException {
        Game currentGame = DataSourceMapper.entityToGame(gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("No such element with given UUID")));
        GameStatus necessaryStatus = playerId.equals(currentGame.getOwnerID()) ? GameStatus.OWNER_TURN : GameStatus.OPPONENT_TURN;
        return currentGame.getCurrentGameStatus().equals(necessaryStatus) && currentGame.getField().getField()[i][j] == 0;
    }

    @Override
    public boolean hasOnlineGame(UUID userId) {
        return gameRepository.findCurrentMultiplayerGame(userId).isPresent();
    }

    @Override
    public void makeTurn(UUID gameId, UUID playerId, int i, int j) throws NoSuchElementException {
        Game currentGame = DataSourceMapper.entityToGame(gameRepository.findById(gameId)
                .orElseThrow(() -> new NoSuchElementException("No such element with given UUID")));
        int playerMark = playerId.equals(currentGame.getOwnerID()) ? currentGame.getOwnerMark() : currentGame.getOpponentMark();
        currentGame.getField().getField()[i][j] = playerMark;
        currentGame.setCurrentGameStatus(checkGameStatus(currentGame));
        gameRepository.save(DataSourceMapper.gameToEntity(currentGame));
        eventNotifier.publishStateEvent(new StateEvent(this, gameId));
    }

    @Override
    public Collection<Game> getAvailableMultiplayerGamesFor(UUID ownerId) {
        return gameRepository.findAvailableMultiplayerGames(ownerId)
                .stream().map(DataSourceMapper::entityToGame).collect(Collectors.toList());
    }

    @Override
    public Collection<Game> getCompletedGames(UUID userId) {
        return gameRepository.findCompletedGames(userId).stream()
                .map(DataSourceMapper::entityToGame).collect(Collectors.toList());
    }

    @Override
    public Collection<Game> getUnfinishedGames(UUID userId) {
        return gameRepository.findUnfinishedGames(userId).stream()
                .map(DataSourceMapper::entityToGame).collect(Collectors.toList());
    }

    @Override
    public Collection<StatisticModel> getBestPlayerStatistics(int numberOfPlayers) {
        return gameRepository.findBestPlayers(numberOfPlayers).stream()
                .map(DataSourceMapper::entityToStatistic).collect(Collectors.toList());
    }

    private void computerStep(Game game) {
        int bestScore = Integer.MIN_VALUE;
        int[] bestStep = null;
        int[][] field = game.getField().getField();
        List<int[]> moves = getAvailableTurns(game.getField());

        for (var move : moves) {
            field[move[0]][move[1]] = game.getOpponentMark();
            int moveValue = minimax(game, 0, false);
            field[move[0]][move[1]] = 0;

            if (moveValue > bestScore) {
                bestScore = moveValue;
                bestStep = move;
            }
        }

        if (bestStep != null) {
            field[bestStep[0]][bestStep[1]] = game.getOpponentMark();
        }

        game.setCurrentGameStatus(checkGameStatus(game));
    }

    private GameStatus checkGameStatus(Game game) {
        int[][] gameField = game.getField().getField();
        int size = gameField.length;

        for (int[] ints : gameField) {
            int value = ints[0];
            if (value != 0) {
                boolean winCondition = true;
                for (int col = 1; col < size; ++col) {
                    if (ints[col] != value) {
                        winCondition = false;
                        break;
                    }
                }
                if (winCondition) {
                    return value == game.getOwnerMark() ? GameStatus.OWNER_WIN : GameStatus.OPPONENT_WIN;
                }
            }
        }

        for (int col = 0; col < size; ++col) {
            int value = gameField[0][col];
            if (value != 0) {
                boolean winCondition = true;
                for (int row = 1; row < size; ++row) {
                    if (gameField[row][col] != value) {
                        winCondition = false;
                        break;
                    }
                }
                if (winCondition) {
                    return value == game.getOwnerMark() ? GameStatus.OWNER_WIN : GameStatus.OPPONENT_WIN;
                }
            }
        }

        int diagonalValue = gameField[0][0];
        if (diagonalValue != 0) {
            boolean winCondition = true;
            for (int i = 1; i < size; ++i) {
                if (gameField[i][i] != diagonalValue) {
                    winCondition = false;
                    break;
                }
            }
            if (winCondition) {
                return diagonalValue == game.getOwnerMark() ? GameStatus.OWNER_WIN : GameStatus.OPPONENT_WIN;
            }
        }

        int sideDiagonalValue = gameField[0][size - 1];
        if (sideDiagonalValue != 0) {
            boolean winCondition = true;
            for (int i = 1; i < size; ++i) {
                if (gameField[i][size - 1 - i] != sideDiagonalValue) {
                    winCondition = false;
                    break;
                }
            }
            if (winCondition) {
                return sideDiagonalValue == game.getOwnerMark() ? GameStatus.OWNER_WIN : GameStatus.OPPONENT_WIN;
            }
        }

        if (Arrays.stream(gameField).flatMapToInt(Arrays::stream).anyMatch(value -> value == 0)) {
            return game.getCurrentGameStatus();
        }
        return GameStatus.DRAW;
    }

    private List<int[]> getAvailableTurns(GameField field) {
        int[][] gameField = field.getField();
        int size = gameField.length;
        List<int[]> result = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            for (int j = 0; j < size; ++j) {
                if (gameField[i][j] == 0)
                    result.add(new int[]{i, j});
            }
        }
        return result;
    }

    private int evaluateBoard(GameStatus status) {
        return switch (status) {
            case OPPONENT_WIN -> 1;
            case OWNER_WIN -> -1;
            default -> 0;
        };
    }

    private int minimax(Game game, int depth, boolean isMaximizing) {
        GameStatus result = checkGameStatus(game);
        if (result != GameStatus.OWNER_TURN && result != GameStatus.OPPONENT_TURN) {
            return evaluateBoard(result);
        }

        int[][] gameField = game.getField().getField();
        List<int[]> moves = getAvailableTurns(game.getField());

        int bestScore;
        if (isMaximizing) {
            bestScore = Integer.MIN_VALUE;
            for (var move : moves) {
                gameField[move[0]][move[1]] = game.getOpponentMark();
                int score = minimax(game, depth + 1, false);
                gameField[move[0]][move[1]] = 0;
                bestScore = Math.max(bestScore, score);
            }
        } else {
            bestScore = Integer.MAX_VALUE;
            for (var move : moves) {
                gameField[move[0]][move[1]] = game.getOwnerMark();
                int score = minimax(game, depth + 1, true);
                gameField[move[0]][move[1]] = 0;
                bestScore = Math.min(bestScore, score);
            }
        }
        return bestScore;
    }
}

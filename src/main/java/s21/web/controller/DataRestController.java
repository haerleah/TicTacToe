package s21.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import s21.di.infrastructure.EventNotifier;
import s21.domain.model.GameStatus;
import s21.domain.service.GameService;
import s21.domain.service.UserService;
import s21.security.model.JwtAuthentication;
import s21.security.service.AuthorizationService;
import s21.web.mapper.WebMapper;
import s21.web.model.GameDTO;
import s21.web.model.StatisticModelDTO;
import s21.web.model.UserDTO;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
public class DataRestController {
    private final GameService gameService;
    private final UserService userService;
    private final EventNotifier eventNotifier;
    private final AuthorizationService authorizationService;

    @Autowired
    public DataRestController(GameService service, UserService userService, EventNotifier eventNotifier, AuthorizationService authorizationService) {
        this.gameService = service;
        this.userService = userService;
        this.eventNotifier = eventNotifier;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/game/{uuid}")
    public ResponseEntity<Void> processPostRequest(@PathVariable("uuid") UUID uuid,
                                                   @RequestParam("row") int row,
                                                   @RequestParam("col") int col) {
        try {
            JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
            if (!gameService.validateTurn(row, col, uuid, (UUID) jwtAuthentication.getPrincipal())) {
                return ResponseEntity.badRequest().build();
            }
            gameService.makeTurn(uuid, (UUID) jwtAuthentication.getPrincipal(), row, col);
            gameService.nextStep(uuid);
            return ResponseEntity.ok().build();
        } catch (NoSuchElementException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/game/{uuid}/stream")
    public SseEmitter startGameStream(@PathVariable("uuid") UUID gameId) {
        SseEmitter emitter = new SseEmitter(0L);
        ExecutorService sseExecutor = Executors.newSingleThreadExecutor();
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        sseExecutor.execute(() -> {
            try {
                UUID userId = (UUID) jwtAuthentication.getPrincipal();
                while (!sseExecutor.isShutdown()) {
                    GameDTO currentGame = WebMapper.gameToDTO(gameService.getGame(gameId));
                    String convertedField = Arrays.stream(currentGame.getField().getField())
                            .flatMapToInt(Arrays::stream)
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(","));

                    String updatedData = convertedField + "\n" + getStatusMessage(gameId);
                    emitter.send(SseEmitter.event()
                            .name("updatedData")
                            .data(updatedData, MediaType.TEXT_PLAIN));
                    eventNotifier.getBlockingQueue(gameId, userId).take();
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                sseExecutor.shutdown();
            }
        });
        return emitter;
    }

    @GetMapping("/accessible_multiplayer_games")
    public List<Map<String, String>> showAccessibleMultiplayerGames() {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();

        List<GameDTO> accessibleMultiplayerGames = gameService
                .getAvailableMultiplayerGamesFor((UUID) jwtAuthentication.getPrincipal())
                .stream()
                .map(WebMapper::gameToDTO)
                .toList();

        return accessibleMultiplayerGames.stream()
                .map(gameDTO -> {
                    Map<String, String> map = new TreeMap<>();
                    map.put("login", userService.getUser(gameDTO.getOwnerId()).getLogin());
                    map.put("gameId", gameDTO.getUuid().toString());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/has_online_game")
    public Boolean hasOnlineGame() {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        return gameService.hasOnlineGame((UUID) jwtAuthentication.getPrincipal());
    }

    @GetMapping("/user_data")
    public UserDTO getUserData() {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        UUID userId = (UUID) jwtAuthentication.getPrincipal();
        return WebMapper.userToDTO(userService.getUser(userId));
    }

    @GetMapping("/completed_games")
    public List<Map<String, Object>> getCompletedGames() {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        UUID userId = (UUID) jwtAuthentication.getPrincipal();
        List<GameDTO> completedGames = gameService.getCompletedGames(userId)
                .stream().map(WebMapper::gameToDTO).toList();

        return getGamesData(userId, completedGames);
    }

    @GetMapping("/unfinished_games")
    public List<Map<String, Object>> getUnfinishedGames() {
        JwtAuthentication jwtAuthentication = authorizationService.getAuthentication();
        UUID userId = (UUID) jwtAuthentication.getPrincipal();
        List<GameDTO> completedGames = gameService.getUnfinishedGames(userId)
                .stream().map(WebMapper::gameToDTO).toList();

        return getGamesData(userId, completedGames);
    }

    @GetMapping("/best_players")
    public List<StatisticModelDTO> getBestPlayers(@RequestParam("limit") int numberOfPlayers) {
        List<StatisticModelDTO> statisticsList = gameService.getBestPlayerStatistics(numberOfPlayers)
                .stream().map(WebMapper::statisticToDTO).toList();

        statisticsList.forEach(statisticDTO -> {
            String login = userService.getUser(statisticDTO.getUserId()).getLogin();
            statisticDTO.setLogin(login);
        });

        return statisticsList;
    }

    private String getStatusMessage(UUID gameId) {
        GameStatus gameStatus = gameService.getGame(gameId).getCurrentGameStatus();
        String gameOwnerLogin = userService.getUser(gameService.getGame(gameId).getOwnerID()).getLogin();
        String opponentLogin = "Bot";
        if (gameService.getGame(gameId).getOpponentID() != null) {
            opponentLogin = userService.getUser(gameService.getGame(gameId).getOpponentID()).getLogin();
        }
        return switch (gameStatus) {
            case WAITING_FOR_PLAYERS -> "Waiting another player...";
            case OPPONENT_WIN -> opponentLogin + " wins!";
            case OWNER_WIN -> gameOwnerLogin + " wins!";
            case DRAW -> "Draw!";
            default -> "";
        };
    }

    private List<Map<String, Object>> getGamesData(UUID userId, List<GameDTO> completedGames) {
        return completedGames.stream().map(gameDTO -> {
            Map<String, Object> game = new TreeMap<>();
            String opponent;
            if (userId.equals(gameDTO.getOwnerId())) {
                if (gameDTO.getOpponentId() == null)
                    opponent = "Бота";
                else
                    opponent = userService.getUser(gameDTO.getOpponentId()).getLogin();
            } else {
                opponent = userService.getUser(gameDTO.getOwnerId()).getLogin();
            }
            game.put("opponent", opponent);
            game.put("game", gameDTO);
            return game;
        }).collect(Collectors.toList());
    }
}

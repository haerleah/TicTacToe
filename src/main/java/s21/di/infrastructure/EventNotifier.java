package s21.di.infrastructure;

import s21.datasource.repository.GameRepository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class EventNotifier {
    private final Map<UUID, Map<UUID, BlockingQueue<StateEvent>>> blockingQueuesMap;

    public EventNotifier(GameRepository gameRepository) {
        blockingQueuesMap = new ConcurrentHashMap<>();
        gameRepository.findAll().forEach(game -> {
            createBlockingQueueChunk(game.getUuid(), game.getOwner().getUuid());
            if (game.getOpponent() != null) {
                createBlockingQueueChunk(game.getUuid(), game.getOpponent().getUuid());
            }
        });
    }

    public void createBlockingQueueChunk(UUID gameId, UUID playerId) {
        Map<UUID, BlockingQueue<StateEvent>> blockingQueueChunk;
        if (!blockingQueuesMap.containsKey(gameId)) {
            blockingQueueChunk = new ConcurrentHashMap<>();
            blockingQueuesMap.put(gameId, blockingQueueChunk);
        } else {
            blockingQueueChunk = blockingQueuesMap.get(gameId);
        }
        blockingQueueChunk.put(playerId, new ArrayBlockingQueue<>(1));
    }

    public void publishStateEvent(StateEvent stateEvent) {
        Map<UUID, BlockingQueue<StateEvent>> queueChunk = blockingQueuesMap.get(stateEvent.getIdentifier());
        queueChunk.values().forEach(queue -> {
            if (queue == null)
                throw new IllegalStateException("No queue found for uuid: " + stateEvent.getIdentifier());
            try {
                queue.put(stateEvent);
            } catch (InterruptedException e) {
                throw new RuntimeException("Message putting was interrupted", e);
            }
        });
    }

    public BlockingQueue<StateEvent> getBlockingQueue(UUID gameId, UUID userId) throws NullPointerException {
        return blockingQueuesMap.get(gameId).get(userId);
    }
}

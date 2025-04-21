package s21.datasource.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import s21.datasource.model.GameEntity;
import s21.datasource.model.StatisticModelEntity;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends CrudRepository<GameEntity, UUID> {

    @Query("select g from GameEntity g where g.singlePlayer = false and g.opponent is null and g.owner.uuid <> :ownerUuid")
    Collection<GameEntity> findAvailableMultiplayerGames(@Param("ownerUuid") UUID ownerUuid);

    @Query("""
            select g from GameEntity g
            where g.singlePlayer = false and g.owner.uuid = :ownerUuid
            order by g.creationDate desc limit 1
            """)
    Optional<GameEntity> findCurrentMultiplayerGame(@Param("ownerUuid") UUID ownerUuid);

    @Query("""
            select g from GameEntity g
            where (g.owner.uuid = :ownerUuid or g.opponent.uuid = :ownerUuid)
                         and g.status in ('OWNER_WIN', 'OPPONENT_WIN', 'DRAW')
            order by g.creationDate asc
            """)
    Collection<GameEntity> findCompletedGames(@Param("ownerUuid") UUID ownerUuid);

    @Query("""
            select g from GameEntity g
            where (g.owner.uuid = :ownerUuid or g.opponent.uuid = :ownerUuid)
                         and g.status in ('OWNER_TURN', 'OPPONENT_TURN', 'WAITING_FOR_PLAYERS')
            order by g.creationDate asc
            """)
    Collection<GameEntity> findUnfinishedGames(@Param("ownerUuid") UUID ownerUuid);

    @Query("""
            select new s21.datasource.model.StatisticModelEntity(u.uuid
                       , (sum(case when
                                   (g.status = 'OWNER_WIN' and g.owner.uuid = u.uuid)
                                   or
                                   (g.status = 'OPPONENT_WIN' and g.opponent.uuid = u.uuid)
                                   then 1 else 0 end) * 100.0) / count(*) as winPercentage)
            from UserEntity u
            join GameEntity g on (g.owner = u or g.opponent = u) and g.status in ('OWNER_WIN', 'OPPONENT_WIN', 'DRAW')
            group by 1
            order by 2 desc
            limit :limit
            """)
    Collection<StatisticModelEntity> findBestPlayers(@Param("limit") int limit);
}

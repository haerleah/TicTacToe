package s21.datasource.repository;

import org.springframework.data.repository.CrudRepository;
import s21.datasource.model.RefreshTokenEntity;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByTokenOwner_Uuid(UUID tokenOwnerUuid);
}

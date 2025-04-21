package s21.datasource.repository;

import org.springframework.data.repository.CrudRepository;
import s21.datasource.model.UserEntity;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends CrudRepository<UserEntity, UUID> {
    boolean existsByLogin(String login);

    Optional<UserEntity> findByLoginAndPassword(String login, String password);

}

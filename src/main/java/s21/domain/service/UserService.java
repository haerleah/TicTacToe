package s21.domain.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import s21.domain.model.User;

import java.util.NoSuchElementException;
import java.util.UUID;

public interface UserService {
    void createUser(String login, String password);

    boolean validateLogin(String login);

    User getUser(UUID id) throws NoSuchElementException;

    UUID getUserIdByLoginAndPassword(String login, String password) throws UsernameNotFoundException;

}

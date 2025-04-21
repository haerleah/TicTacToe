package s21.domain.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import s21.datasource.mapper.DataSourceMapper;
import s21.datasource.repository.UserRepository;
import s21.domain.model.User;

import java.util.NoSuchElementException;
import java.util.UUID;

public class UserServiceImplementation implements UserService {
    private final UserRepository userRepository;

    public UserServiceImplementation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void createUser(String login, String password) {
        userRepository.save(DataSourceMapper.userToEntity(new User(login, password)));
    }

    @Override
    public boolean validateLogin(String login) {
        return !userRepository.existsByLogin(login);
    }

    @Override
    public User getUser(UUID id) throws NoSuchElementException {
        return DataSourceMapper.entityToUser(userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("No such element with given UUID")));
    }

    @Override
    public UUID getUserIdByLoginAndPassword(String login, String password) throws UsernameNotFoundException {
        if (userRepository.findByLoginAndPassword(login, password).isPresent()) {
            return userRepository.findByLoginAndPassword(login, password).get().getUuid();
        }
        throw new UsernameNotFoundException("There is no user with given login and password");
    }
}

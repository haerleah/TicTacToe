package s21.security.service;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import s21.security.model.JwtAuthentication;
import s21.security.model.JwtRequest;
import s21.security.model.JwtResponse;

import javax.security.auth.RefreshFailedException;
import java.util.NoSuchElementException;

public interface AuthorizationService {
    boolean registerUser(JwtRequest request);

    JwtResponse authorizeUser(JwtRequest request) throws UsernameNotFoundException;

    JwtResponse updateAccessToken(String refreshToken) throws RefreshFailedException, UsernameNotFoundException;

    JwtResponse updateRefreshToken(String refreshToken) throws RefreshFailedException, NoSuchElementException;

    JwtAuthentication getAuthentication();

    int getTokenLifespan(String token);
}

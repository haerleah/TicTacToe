package s21.security.service;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import s21.datasource.mapper.DataSourceMapper;
import s21.datasource.model.RefreshTokenEntity;
import s21.datasource.repository.RefreshTokenRepository;
import s21.domain.model.User;
import s21.domain.service.UserService;
import s21.security.model.*;
import s21.security.util.JwtProvider;

import javax.security.auth.RefreshFailedException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

public class AuthorizationServiceImplementation implements AuthorizationService {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthorizationServiceImplementation(UserService userService, JwtProvider jwtProvider, RefreshTokenRepository refreshTokenRepository) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public boolean registerUser(JwtRequest request) {
        if (userService.validateLogin(request.getLogin())) {
            userService.createUser(request.getLogin(), request.getPassword());
            return true;
        }
        return false;
    }

    @Override
    public JwtResponse authorizeUser(JwtRequest request) throws UsernameNotFoundException {
        UUID userId = userService.getUserIdByLoginAndPassword(request.getLogin(), request.getPassword());
        User user = userService.getUser(userId);
        String accessToken = jwtProvider.generateAccessToken(user);
        String refreshToken = jwtProvider.generateRefreshToken(user);

        saveOrUpdateRefreshToken(refreshToken, userId);

        return new JwtResponse(accessToken, refreshToken);
    }

    @Override
    public JwtResponse updateAccessToken(String refreshToken) throws RefreshFailedException, UsernameNotFoundException {
        if (!jwtProvider.validateRefreshToken(refreshToken))
            throw new RefreshFailedException("Invalid Refresh-token");
        Claims refreshTokenClaims = jwtProvider.getClaimsFromToken(refreshToken);
        User user = userService.getUser(UUID.fromString(refreshTokenClaims.getSubject()));
        String accessToken = jwtProvider.generateAccessToken(user);
        String newRefreshToken = jwtProvider.generateRefreshToken(refreshToken);

        saveOrUpdateRefreshToken(newRefreshToken, user.getUuid());

        return new JwtResponse(accessToken, newRefreshToken);
    }

    @Override
    public JwtResponse updateRefreshToken(String refreshToken) throws RefreshFailedException, NoSuchElementException {
        if (!jwtProvider.validateRefreshToken(refreshToken))
            throw new RefreshFailedException("Invalid Refresh-token");

        Claims refreshTokenClaims = jwtProvider.getClaimsFromToken(refreshToken);
        User user = userService.getUser(UUID.fromString(refreshTokenClaims.getSubject()));
        String updatedRefreshToken = jwtProvider.generateRefreshToken(user);
        String accessToken = jwtProvider.generateAccessToken(user);

        saveOrUpdateRefreshToken(updatedRefreshToken, user.getUuid());

        return new JwtResponse(accessToken, updatedRefreshToken);
    }

    @Override
    public JwtAuthentication getAuthentication() {
        return (JwtAuthentication) SecurityContextHolder.getContext().getAuthentication();
    }

    @Override
    public int getTokenLifespan(String token) {
        Claims claims = jwtProvider.getClaimsFromToken(token);
        Date issuedAt = claims.getIssuedAt();
        Date expiredAt = claims.getExpiration();
        return (int) ((expiredAt.getTime() - issuedAt.getTime()) / 1000);
    }

    private void saveOrUpdateRefreshToken(String refreshToken, UUID userId) {
        Optional<RefreshTokenEntity> existingTokenOpt = refreshTokenRepository.findByTokenOwner_Uuid(userId);
        if (existingTokenOpt.isPresent()) {
            RefreshTokenEntity existingToken = existingTokenOpt.get();
            existingToken.setToken(refreshToken);
            refreshTokenRepository.save(existingToken);
        } else {
            RefreshToken token = new RefreshToken();
            token.setTokenOwnerUuid(userId);
            token.setToken(refreshToken);
            refreshTokenRepository.save(DataSourceMapper.tokenToEntity(token));
        }
    }
}

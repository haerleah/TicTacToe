package s21.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import s21.datasource.model.RefreshTokenEntity;
import s21.datasource.repository.RefreshTokenRepository;
import s21.domain.model.User;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class JwtProvider {
    private final SecretKey secretKey;
    private final RefreshTokenRepository tokenRepository;

    public JwtProvider(SecretKey secretKey, RefreshTokenRepository tokenRepository) {
        this.secretKey = secretKey;
        this.tokenRepository = tokenRepository;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofMinutes(15));
        return Jwts.builder()
                .claims()
                .subject(user.getUuid().toString())
                .issuedAt(new Date(now.toEpochMilli()))
                .expiration(new Date(expiresAt.toEpochMilli()))
                .and()
                .claim("tokenType", "access")
                .claim("roles", user.getRoles())
                .signWith(secretKey) // в ключ занесена информация об алгоритме шифрования (HS256)
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofDays(1));
        return Jwts.builder()
                .claims()
                .subject(user.getUuid().toString())
                .issuedAt(new Date(now.toEpochMilli()))
                .expiration(new Date(expiresAt.toEpochMilli()))
                .and()
                .claim("tokenType", "refresh")
                .signWith(secretKey) // в ключ занесена информация об алгоритме шифрования (HS256)
                .compact();
    }

    public String generateRefreshToken(String refreshToken) {
        Instant now = Instant.now();
        Claims claims = getClaimsFromToken(refreshToken);
        return Jwts.builder()
                .claims()
                .subject(claims.getSubject())
                .issuedAt(new Date(now.toEpochMilli()))
                .expiration(claims.getExpiration())
                .and()
                .claim("tokenType", "refresh")
                .signWith(secretKey) // в ключ занесена информация об алгоритме шифрования (HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!claims.get("tokenType").equals("access")) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean validateRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!claims.get("tokenType").equals("refresh")) {
                return false;
            }

            UUID tokenOwnerId = UUID.fromString(claims.getSubject());
            Optional<RefreshTokenEntity> tokenFromDatabase = tokenRepository.findByTokenOwner_Uuid(tokenOwnerId);
            if (tokenFromDatabase.isEmpty()) {
                return false;
            }

            String tokenStringFromDatabase = tokenFromDatabase.get().getToken();
            if (!tokenStringFromDatabase.equals(token)) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public Claims getClaimsFromToken(String token) {
        Claims parsedClaims;
        try {
            parsedClaims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException exception) {
            parsedClaims = exception.getClaims();
        }
        return parsedClaims;
    }
}


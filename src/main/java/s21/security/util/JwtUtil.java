package s21.security.util;

import io.jsonwebtoken.Claims;
import s21.domain.model.Role;
import s21.security.model.JwtAuthentication;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class JwtUtil {
    public JwtAuthentication createJwtAuthentication(Claims claims) {
        JwtAuthentication jwtAuthentication;
        List<?> authorities;
        UUID userId = UUID.fromString(claims.getSubject());
        if (claims.containsKey("roles")) {
            authorities = claims.get("roles", List.class);
            Set<Role> roles = authorities.stream().map(obj -> Role.valueOf((String) obj)).collect(Collectors.toSet());
            jwtAuthentication = new JwtAuthentication(userId, null, roles);
        } else {
            jwtAuthentication = new JwtAuthentication(userId, null);
        }
        return jwtAuthentication;
    }
}

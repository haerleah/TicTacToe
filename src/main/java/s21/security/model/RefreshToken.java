package s21.security.model;

import java.util.UUID;

public class RefreshToken {
    private UUID tokenOwnerUuid;
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getTokenOwnerUuid() {
        return tokenOwnerUuid;
    }

    public void setTokenOwnerUuid(UUID tokenOwnerUuid) {
        this.tokenOwnerUuid = tokenOwnerUuid;
    }
}

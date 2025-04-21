package s21.web.model;

import java.util.UUID;

public class UserDTO {
    private UUID uuid;
    private String login;

    public UserDTO() {
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}

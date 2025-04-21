package s21.domain.model;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class User {
    private UUID uuid;
    private String login;
    private String password;
    private Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String login, String password) {
        this.uuid = UUID.randomUUID();
        this.login = login;
        this.password = password;
        this.roles.add(Role.USER);
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> role) {
        this.roles = role;
    }
}

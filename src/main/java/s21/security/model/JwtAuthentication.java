package s21.security.model;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import s21.domain.model.Role;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class JwtAuthentication implements Authentication {
    private boolean authenticated;
    private final UUID principal;
    private final String credentials;
    private final Set<Role> roles;

    public JwtAuthentication(UUID principal, String credentials) {
        this.principal = principal;
        this.credentials = credentials;
        this.roles = new HashSet<>();
        this.authenticated = false;
    }

    public JwtAuthentication(UUID principal, String credentials, Collection<Role> roles) {
        this.principal = principal;
        this.credentials = credentials;
        this.roles = new HashSet<>(roles);
        this.authenticated = true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JwtAuthentication test)) {
            return false;
        }
        if (!this.roles.equals(test.roles)) {
            return false;
        }
        if ((this.getCredentials() == null) && (test.getCredentials() != null)) {
            return false;
        }
        if ((this.getCredentials() != null) && !this.getCredentials().equals(test.getCredentials())) {
            return false;
        }
        if (this.getPrincipal() == null && test.getPrincipal() != null) {
            return false;
        }
        if (this.getPrincipal() != null && !this.getPrincipal().equals(test.getPrincipal())) {
            return false;
        }
        return this.isAuthenticated() == test.isAuthenticated();
    }

    @Override
    public int hashCode() {
        int code = 31;
        for (GrantedAuthority authority : this.roles) {
            code ^= authority.hashCode();
        }
        if (this.getPrincipal() != null) {
            code ^= this.getPrincipal().hashCode();
        }
        if (this.getCredentials() != null) {
            code ^= this.getCredentials().hashCode();
        }
        if (this.isAuthenticated()) {
            code ^= -37;
        }
        return code;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName()).append(" [");
        stringBuilder.append("Principal=").append(getPrincipal()).append(", ");
        stringBuilder.append("Credentials=[PROTECTED], ");
        stringBuilder.append("Authenticated=").append(isAuthenticated()).append(", ");
        stringBuilder.append("Granted Authorities=").append(this.roles);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public String getName() {
        return principal.toString();
    }
}

package s21.datasource.model;

import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(
            name = "token_owner",
            referencedColumnName = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_refresh_token_owner")
    )
    private UserEntity tokenOwner;

    public RefreshTokenEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserEntity getTokenOwner() {
        return tokenOwner;
    }

    public void setTokenOwner(UserEntity tokenOwner) {
        this.tokenOwner = tokenOwner;
    }
}
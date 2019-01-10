package exchange.velox.authservice.domain;

import exchange.velox.authservice.dto.TokenType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "passwordToken")
public class PasswordToken {
    private String id;
    private String email;
    private String token;
    private Long timestamp;
    private TokenType tokenType;

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
                name = "UUID",
                strategy = "org.hibernate.id.UUIDGenerator")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(unique = true, nullable = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(unique = true, nullable = false, length = 200)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    public TokenType getTokenType() {
        return tokenType;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    @Column
    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

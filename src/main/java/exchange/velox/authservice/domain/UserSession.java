package exchange.velox.authservice.domain;

import exchange.velox.authservice.dto.ClientType;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "userSession")
public class UserSession {
    private String id;
    private String userId;
    private String token;
    private ClientType clientType;
    private Long expireDate;
    private String userAgent;

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

    @Column(unique = true, nullable = false, length = 200)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Column
    public Long getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Long expireDate) {
        this.expireDate = expireDate;
    }

    @Column(nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    public ClientType getClientType() {
        return clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    @Column(nullable = false)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Column(length = 512)
    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}


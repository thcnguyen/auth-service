package exchange.velox.authservice.domain;

import java.util.HashSet;
import java.util.Set;

public class UserDTO {
    public static final int MAX_LOGIN_ATTEMPTS = 5;

    private String id;
    private String email;
    private String password;
    private String role;
    private Boolean active;
    private Long lastLogin;
    private Set<String> permissions = new HashSet<>();

    private int loginAttempt = 0;

    public int getLoginAttempt() {
        return loginAttempt;
    }

    public void setLoginAttempt(int loginAttempt) {
        this.loginAttempt = loginAttempt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void increaseLoginAttempt() {
        ++loginAttempt;
    }

    public boolean hasRemainingLoginAttempts(){
        return getLoginAttempt() <= MAX_LOGIN_ATTEMPTS;
    }

    public int getRemainingLoginAttempts() {
        return MAX_LOGIN_ATTEMPTS - getLoginAttempt();
    }

    public Boolean isActive() {
        return active;
    }

    public void resetLoginAttempt() {
        loginAttempt = 0;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}


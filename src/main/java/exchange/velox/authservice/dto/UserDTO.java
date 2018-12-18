package exchange.velox.authservice.dto;

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
    private String lang;
    private Set<String> permissions = new HashSet<>();
    private String firstname;
    private String lastname;
    private String searchableId;

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

    public boolean hasRemainingLoginAttempts() {
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

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFullName() {
        return getFirstname() + " " + getLastname();
    }

    public String getSearchableId() {
        return searchableId;
    }

    public void setSearchableId(String searchableId) {
        this.searchableId = searchableId;
    }

    public String getHumanId() {
        if (getSearchableId() == null) {
            return null;
        }
        String prefix = getPrefixByRole(role);
        return String.format("%s%s", prefix, getSearchableId());
    }

    public static String getPrefixByRole(String role) {
        if (UserRole.SELLER_COMPANY.name().equals(role)) {
            return "S";
        } else if (UserRole.BIDDER_COMPANY.name().equals(role)) {
            return "B";
        } else if (UserRole.INTRODUCER.name().equals(role)) {
            return "I";
        } else if (UserRole.SELLER.name().equals(role) | UserRole.BIDDER.name().equals(role)) {
            return "E";
        }
        return null;
    }
}


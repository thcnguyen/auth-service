package exchange.velox.authservice.dto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserSessionDTO {
    private String id;
    private String email;
    private String role;
    private String token;
    private Set<String> permissions = new HashSet<>();
    private Map<String, Object> extraData = new HashMap<>();
    private Boolean totpRequiredAtLogin;
    private String companyId;

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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(Map<String, Object> extraData) {
        this.extraData = extraData;
    }

    public Boolean getTotpRequiredAtLogin() {
        return totpRequiredAtLogin;
    }

    public void setTotpRequiredAtLogin(Boolean totpRequiredAtLogin) {
        this.totpRequiredAtLogin = totpRequiredAtLogin;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}

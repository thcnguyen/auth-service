package exchange.velox.authservice.service;

import exchange.velox.authservice.dto.UserDTO;
import exchange.velox.authservice.domain.UserSession;
import exchange.velox.authservice.dto.UserSessionDTO;

public class UtilsService {
    public UserDTO mapToUserDTO(Object[] source) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(stringValueOf(source[0]));
        userDTO.setEmail(stringValueOf(source[1]));
        userDTO.setPassword(stringValueOf(source[2]));
        userDTO.setRole(stringValueOf((source[3])));
        userDTO.setActive(Boolean.valueOf((stringValueOf(source[4]))));
        if (source[5] != null) {
            userDTO.setLastLogin(Long.valueOf(stringValueOf(source[5])));
        } else {
            userDTO.setLastLogin(null);
        }
        userDTO.setLoginAttempt(Integer.valueOf(stringValueOf(source[6])));
        userDTO.setLang(stringValueOf(source[7]));
        userDTO.setTotpRequiredAtLogin(Boolean.valueOf(stringValueOf(source[8])));
        return userDTO;
    }

    public UserSessionDTO mapToUserSessionDTO(UserDTO user, UserSession userSession) {
        UserSessionDTO session = new UserSessionDTO();
        session.setId(user.getId());
        session.setEmail(user.getEmail());
        session.setToken(userSession.getToken());
        session.setRole(user.getRole());
        session.setPermissions(user.getPermissions());
        session.setTotpRequiredAtLogin(user.getTotpRequiredAtLogin());
        return session;
    }

    private String stringValueOf(Object obj) {
        if (obj == null) return null;
        return String.valueOf(obj);
    }
}

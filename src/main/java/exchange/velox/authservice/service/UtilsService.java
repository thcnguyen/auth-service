package exchange.velox.authservice.service;

import exchange.velox.authservice.domain.UserDTO;
import exchange.velox.authservice.domain.UserSession;
import exchange.velox.authservice.domain.UserSessionDTO;

public class UtilsService {
    public UserDTO mapToUserDTO(Object[] source) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(String.valueOf(source[0]));
        userDTO.setEmail(String.valueOf(source[1]));
        userDTO.setPassword(String.valueOf(source[2]));
        userDTO.setRole(String.valueOf(source[3]));
        userDTO.setActive(Boolean.valueOf((String.valueOf(source[4]))));
        userDTO.setLastLogin(Long.valueOf(String.valueOf(source[5])));
        userDTO.setLoginAttempt(Integer.valueOf(String.valueOf(source[6])));
        return userDTO;
    }

    public UserSessionDTO mapToUserSessionDTO(UserDTO user, UserSession userSession) {
        UserSessionDTO session = new UserSessionDTO();
        session.setId(user.getId());
        session.setEmail(user.getEmail());
        session.setToken(userSession.getToken());
        session.setRole(user.getRole());
        session.setPermissions(user.getPermissions());
        return session;
    }
}

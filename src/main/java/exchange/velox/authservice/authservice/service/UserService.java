package exchange.velox.authservice.authservice.service;

import exchange.velox.authservice.authservice.dao.UserDAO;
import exchange.velox.authservice.authservice.dao.UserSessionDAO;
import exchange.velox.authservice.authservice.domain.*;
import net.etalia.crepuscolo.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {


    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserSessionDAO userSessionDAO;

    @Autowired
    private AuthService authService;

    @Autowired
    private UtilsService utilsService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int handleUserMaxLogin(String userId) {
        UserDTO userDTO = userDAO.load(userId);
        userDTO.increaseLoginAttempt();
        if (!userDTO.hasRemainingLoginAttempts()) {
            userDTO.setActive(false);
        }
        userDAO.updateUser(userDTO);
        return userDTO.getRemainingLoginAttempts();
    }

    @Transactional
    public UserSession generateNewUserSession(UserDTO userDTO) {
        UserSession userSession = new UserSession();
        userSession.setUid(userDTO.getId());
        // TODO : handle client type
        userSession.setClientType(ClientType.WEB);
        userSession.setExpireDate(System.currentTimeMillis() + authService.getMaxTokenTime());
        userSession.setToken(authService.generateRandomToken());
        return userSessionDAO.save(userSession);
    }

    private UserSession updateCurrentSession(UserSession session) {
        session.setToken(authService.generateRandomToken());
        session.setExpireDate(System.currentTimeMillis() + authService.getMaxTokenTime());
        return userSessionDAO.save(session);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSessionDTO checkValidToken(String token) {
        Optional<UserSession> sessionOpt = userSessionDAO.findUserSessionByToken(token);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            UserDTO user = userDAO.load(session.getUid());
            if (checkTimeValidityCondition(session.getExpireDate()) && checkUserActive(user)) {
                user.setPermissions(userDAO.getPermissionListByUser(user));
                session.setExpireDate(System.currentTimeMillis() + authService.getMaxTokenTime());
                userSessionDAO.save(session);
                return utilsService.mapToUserSessionDTO(user, session);

            }
        }
        return null;
    }

    @Transactional
    public void logout(String token) {
        Optional<UserSession> sessionOpt = userSessionDAO.findUserSessionByToken(token);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            userSessionDAO.delete(session);
        }
    }

    private boolean checkTimeValidityCondition(long timeStamp) {
        return System.currentTimeMillis() <= timeStamp;
    }

    private boolean checkUserActive(UserDTO user) {
        if (user == null || !user.isActive()) {
            return false;
        }

        if (UserRole.SELLER.name().equals(user.getRole()) || UserRole.BIDDER.name().equals(user.getRole())) {
            if (!userDAO.getCompanyStatusByUser(user)) {
                return false;
            }
        }
        return true;
    }
}

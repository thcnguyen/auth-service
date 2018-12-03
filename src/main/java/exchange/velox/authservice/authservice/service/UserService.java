package exchange.velox.authservice.authservice.service;

import exchange.velox.authservice.authservice.config.AuthConfig;
import exchange.velox.authservice.authservice.dao.PasswordTokenDAO;
import exchange.velox.authservice.authservice.dao.UserDAO;
import exchange.velox.authservice.authservice.dao.UserSessionDAO;
import exchange.velox.authservice.authservice.domain.*;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.utils.HandledHttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private PasswordTokenDAO passwordTokenDAO;

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
        userSession.setUserId(userDTO.getId());
        // TODO : handle client type
        userSession.setClientType(ClientType.WEB);
        userSession.setExpireDate(System.currentTimeMillis() + AuthConfig.MAX_TOKEN_TIME);
        userSession.setToken(authService.generateRandomToken());
        return userSessionDAO.save(userSession);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserSessionDTO checkValidToken(String token) {
        Optional<UserSession> sessionOpt = userSessionDAO.findUserSessionByToken(token);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            UserDTO user = userDAO.load(session.getUserId());
            if (checkTimeValidityCondition(session.getExpireDate()) && isUserCompanyActive(user)) {
                user.setPermissions(userDAO.getPermissionListByUser(user));
                session.setExpireDate(System.currentTimeMillis() + AuthConfig.MAX_TOKEN_TIME);
                userSessionDAO.save(session);
                return utilsService.mapToUserSessionDTO(user, session);
            } else {
                userSessionDAO.delete(session);
                throw new HandledHttpException().statusCode(HttpStatus.UNAUTHORIZED).errorCode("TOKEN_EXPIRED")
                            .message("The access token expired");
            }
        }
        return null;
    }

    @Transactional
    public UserDTO findUserByEmail(String email) {
        return userDAO.findUserByEmail(email);
    }

    @Transactional
    public UserSessionDTO updateUserAndGenerateSession(UserDTO user) {
        user.setLastLogin(System.currentTimeMillis());
        user.resetLoginAttempt();
        user.setPermissions(userDAO.getPermissionListByUser(user));
        userDAO.updateUser(user);
        UserSession session = generateNewUserSession(user);
        return utilsService.mapToUserSessionDTO(user, session);
    }

    public boolean isUserApproved(UserDTO user) {
        String approvationStep = userDAO.getUserApprovationStep(user);
        if (UserRole.BIDDER.name().equals(user.getRole()) || UserRole.SELLER.name().equals(user.getRole())
                    || UserRole.INTRODUCER.name().equals(user.getRole())) {
            if (ApprovationStep.APPROVED.name().equals(approvationStep)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isUserInitiated(UserDTO user) {
        return userDAO.isUserInitiated(user);
    }

    @Transactional
    public void logout(String token) {
        Optional<UserSession> sessionOpt = userSessionDAO.findUserSessionByToken(token);
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            userSessionDAO.delete(session);
        }
    }

    @Transactional
    public PasswordToken generateForgottenPasswordToken(String email, String userId) {
        PasswordToken pwdToken = new PasswordToken();
        pwdToken.setEmail(email);
        pwdToken.setToken(authService.generateRandomToken());
        pwdToken.setExpireDate(System.currentTimeMillis() + AuthConfig.FORGOT_PASSWORD_EXPIRY_IN_MILLISECONDS);
        pwdToken.setTokenType(TokenType.FORGOT_PWD);
        pwdToken.setUserId(userId);
        return passwordTokenDAO.save(pwdToken);
    }

    private boolean checkTimeValidityCondition(long timeStamp) {
        return System.currentTimeMillis() <= timeStamp;
    }

    public boolean isUserCompanyActive(UserDTO user) {
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

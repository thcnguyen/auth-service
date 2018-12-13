package exchange.velox.authservice.service;

import exchange.velox.authservice.config.AuthConfig;
import exchange.velox.authservice.dao.PasswordTokenDAO;
import exchange.velox.authservice.dao.UserDAO;
import exchange.velox.authservice.dao.UserSessionDAO;
import exchange.velox.authservice.domain.PasswordToken;
import exchange.velox.authservice.domain.UserSession;
import exchange.velox.authservice.dto.*;
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
    private TokenService tokenService;

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
        userSession.setExpireDate(System.currentTimeMillis() + AuthConfig.MAX_SESSION_TIME);
        userSession.setToken(tokenService.generateRandomToken());
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
                session.setExpireDate(System.currentTimeMillis() + AuthConfig.MAX_SESSION_TIME);
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
        UserDTO user = userDAO.findUserByEmail(email);
        if (user != null) {
            user = userDAO.enrichUserInfo(user);
        }
        return user;
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

    @Transactional
    public UserDTO updateUser(UserDTO user) {
        userDAO.updateUser(user);
        return userDAO.findUserByEmail(user.getEmail());
    }

    @Transactional
    public UserSessionDTO generateForgottenPasswordSession(UserDTO user) {
        user.setPermissions(userDAO.getPermissionListByUser(user));
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
        } else {
            throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public PasswordToken generateForgottenPasswordToken(String email) {
        return generatePasswordToken(email, TokenType.FORGOT_PWD);
    }

    @Transactional
    public PasswordToken generateInviteSellerUserPasswordToken(String email) {
        return generatePasswordToken(email, TokenType.SELLER_USER_INVITE);
    }

    @Transactional
    public PasswordToken generateInviteBidderUserPasswordToken(String email) {
        return generatePasswordToken(email, TokenType.BIDDER_USER_INVITE);
    }

    private PasswordToken generatePasswordToken(String email, TokenType type) {
        PasswordToken pwdToken = passwordTokenDAO.findPasswordTokenByEmail(email);
        if (pwdToken != null) {
            pwdToken.setToken(tokenService.generateRandomToken());
            pwdToken.setTimestamp(System.currentTimeMillis());
            pwdToken.setTokenType(type);
        } else {
            pwdToken = new PasswordToken();
            pwdToken.setEmail(email);
            pwdToken.setToken(tokenService.generateRandomToken());
            pwdToken.setTimestamp(System.currentTimeMillis());
            pwdToken.setTokenType(type);
        }
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

    @Transactional
    public String updateForgottenPassword(UserDTO user, boolean isInitiating) {
        String companyName = null;
        if (isInitiating) {
            if (UserRole.SELLER.name().equals(user.getRole()) || UserRole.BIDDER.name().equals(user.getRole())) {
                user.setActive(Boolean.TRUE);
                userDAO.updateUser(user);
                userDAO.updateInitiatedStatus(user, Boolean.TRUE);
                companyName = userDAO.getUserCompanyName(user);
            }
        } else {
            if (user.isActive()) {
                userDAO.updateUser(user);
            } else {
                user.setActive(Boolean.TRUE);
                userDAO.updateUser(user);
            }
        }
        return companyName;
    }

    public String getUserCompanyName(UserDTO user) {
        return userDAO.getUserCompanyName(user);
    }

}

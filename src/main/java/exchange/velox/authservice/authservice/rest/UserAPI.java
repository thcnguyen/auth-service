package exchange.velox.authservice.authservice.rest;

import exchange.velox.authservice.authservice.config.AuthConfig;
import exchange.velox.authservice.authservice.dao.UserDAO;
import exchange.velox.authservice.authservice.dao.UserSessionDAO;
import exchange.velox.authservice.authservice.domain.UserDTO;
import exchange.velox.authservice.authservice.domain.UserSession;
import exchange.velox.authservice.authservice.domain.UserSessionDTO;
import exchange.velox.authservice.authservice.service.UserService;
import exchange.velox.authservice.authservice.service.UtilsService;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.utils.HandledHttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserAPI {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private UserSessionDAO userSessionDAO;

    @Autowired
    private UserService userService;

    @Autowired
    private UtilsService utilsService;

    @Autowired
    AuthService authService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @Transactional
    public UserSessionDTO login(@RequestHeader("Authorization") String authorization) {
        if (!authorization.split("_", 2)[0].equals(AuthConfig.AUTHENTICATION_SIGN)) {
            throw new HandledHttpException().statusCode(HttpStatus.UNAUTHORIZED).errorCode("AUTH_ERROR")
                        .message("Header with invalid realm");
        }
        String[] authParts = authorization.split("_", 2)[1].split(":");
        String email = authParts[0];
        String passMd5 = authParts[1]; // this password is built by: System.out.println(new String(Base64.getEncoder().encode(DigestUtils.md5("PLAIN PASSWORD")), "UTF-8"));
        UserDTO user = userDAO.findUserByEmail(email);
        if (user == null)
            throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND).errorCode("NOTFOUND")
                        .message("Cannot find user with email " + email);
        if (user.getPassword() == null) {
            throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND).errorCode("NOTFOUND")
                        .message("Your account is not initiated");
        }

        if (!authService.verifyPassword(user.getPassword(), passMd5)) {
            int remainingLoginAttempts = userService.handleUserMaxLogin(user.getId());
            if (remainingLoginAttempts < 0) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("User disabled");
            } else {
                throw new HandledHttpException().statusCode(HttpStatus.UNAUTHORIZED).errorCode("WRONGPASSWORD")
                            .message("Wrong password").property("REMAINING_ATTEMPTS", remainingLoginAttempts);
            }
        }
        if (!user.isActive()) {
            throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("NOTVALIDATED")
                        .message("User not validated");
        }
        user.setLastLogin(System.currentTimeMillis());
        user.resetLoginAttempt();
        user.setPermissions(userDAO.getPermissionListByUser(user));
        userDAO.updateUser(user);
        UserSession session = userService.generateNewUserSession(user);
        return utilsService.mapToUserSessionDTO(user, session);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization);
        return (ResponseEntity<?>) ResponseEntity.ok();
    }

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    public ResponseEntity<?> checkValidToken(@RequestHeader("Authorization") String authorization) {
        UserSessionDTO session = userService.checkValidToken(authorization);
        if (session != null) {
            return ResponseEntity.ok(userService.checkValidToken(authorization));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Token invalid");
    }
}

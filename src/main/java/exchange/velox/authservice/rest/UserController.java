package exchange.velox.authservice.rest;

import exchange.velox.authservice.config.AuthConfig;
import exchange.velox.authservice.domain.PasswordToken;
import exchange.velox.authservice.domain.UserDTO;
import exchange.velox.authservice.domain.UserRole;
import exchange.velox.authservice.domain.UserSessionDTO;
import exchange.velox.authservice.service.TokenService;
import exchange.velox.authservice.service.UserService;
import io.swagger.annotations.*;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.utils.HandledHttpException;
import net.etalia.crepuscolo.utils.Strings;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class UserController implements UserAPI {

    private Logger log = LogManager.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ApiOperation(value = "Login")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Successfully login"),
                @ApiResponse(code = 401, message = "You are not authorized to login"),
                @ApiResponse(code = 404, message = "User is not found")
    })
    @ApiImplicitParams({
                @ApiImplicitParam(name = "Authorization", paramType = "header")
    })
    public UserSessionDTO login(@RequestHeader("Authorization") String authorization) {
        if (!authorization.split("_", 2)[0].equals(AuthConfig.AUTHENTICATION_SIGN)) {
            throw new HandledHttpException().statusCode(HttpStatus.UNAUTHORIZED).errorCode("AUTH_ERROR")
                        .message("Header with invalid realm");
        }
        String[] authParts = authorization.split("_", 2)[1].split(":");
        String email = authParts[0];
        String passMd5 = authParts[1]; // this password is built by: System.out.println(new String(Base64.getEncoder().encode(DigestUtils.md5("PLAIN PASSWORD")), "UTF-8"));
        UserDTO user = userService.findUserByEmail(email);
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
            if (!userService.isUserApproved(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("User disabled");
            } else {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("NOTVALIDATED")
                            .message("User not validated");
            }
        }

        if (UserRole.SELLER.name().equals(user.getRole())) {
            if (!userService.isUserApproved(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("Seller not validated");
            }
            if (!userService.isUserCompanyActive(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("Seller disabled");
            }
        }
        if (UserRole.BIDDER.name().equals(user.getRole())) {
            if (!userService.isUserApproved(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("Bidder not validated");
            }
            if (!userService.isUserCompanyActive(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("Bidder disabled");
            }
        }

        return userService.updateUserAndGenerateSession(user);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ApiOperation(value = "Logout")
    @ApiResponses(value = {
                @ApiResponse(code = 204, message = "Logout login")
    })
    @ApiImplicitParams({
                @ApiImplicitParam(name = "Authorization", paramType = "header")
    })
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @RequestMapping(value = "/token", method = RequestMethod.GET)
    @ApiOperation(value = "Check valid token")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "The access token is valid"),
                @ApiResponse(code = 401, message = "The access token expired")
    })
    @ApiImplicitParams({
                @ApiImplicitParam(name = "Authorization", paramType = "header")
    })
    public UserSessionDTO checkValidToken(@RequestHeader("Authorization") String authorization) {
        UserSessionDTO session = userService.checkValidToken(authorization);
        if (session != null) {
            return session;
        }
        throw new HandledHttpException().statusCode(HttpStatus.UNAUTHORIZED).message("Invalid Token");
    }

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.POST)
    @ApiOperation(value = "Forgot password")
    @ApiResponses(value = {
                @ApiResponse(code = 204, message = "The email forgot password has been send to user"),
                @ApiResponse(code = 500, message = "User is not found"),
    })
    public @ResponseStatus(HttpStatus.NO_CONTENT)
    void forgotPassword(
                @ApiParam(value = "Data model need to be forgot password", required = true) @RequestBody Map<String, String> data) {
        if (!data.containsKey("email")) {
            throw new HandledHttpException().statusCode(500).message("Body request does not contain 'email'");
        }
        String email = data.get("email");
        UserDTO user = userService.findUserByEmail(email);
        if (user == null) {
            log.info("User for email {} not found!", email);
            return;
        }
        if (!userService.isUserApproved(user)) {
            log.info("We do not allow to forgot password for WAITING user: {}", email);
            throw new HandledHttpException().statusCode(500).message("Please continue the registration process");
        }
        if (!userService.isUserInitiated(user)) {
            log.info("We do not allow to forgot password for WAITING user: {}", email);
            throw new HandledHttpException().statusCode(500)
                        .message("This user has not been initiated, please continue the registration process");
        }
        PasswordToken pwdToken = userService.generateForgottenPasswordToken(email, user.getId());
        // TODO: Send email forgot password

    }

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.PUT)
    @ApiOperation(value = "Update forgotten password")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Successfully update forgotten password"),
                @ApiResponse(code = 401, message = "You are not authorized"),
                @ApiResponse(code = 400, message = "Missing parameter 'password'")
    })
    public UserSessionDTO updateForgottenPassword(
                @ApiParam(value = "Data model need to be update forgot password", required = true) @RequestBody Map<String, String> data) {
        String password = data.get("password");
        if (Strings.nullOrBlank(password)) {
            log.info("Missing parameter 'password'");
            throw new HandledHttpException().statusCode(HttpStatus.BAD_REQUEST).message("Missing parameter 'password'");
        }

        String token = data.get("token");
        String initiating = data.get("initiating");

        boolean isInitiating = false;
        UserDTO user;
        if (StringUtils.equals(initiating, "SELLER")) {
            user = tokenService.validateSellerRegistrationTokenAndGetUser(token);
            isInitiating = true;
        } else if (StringUtils.equals(initiating, "BIDDER")) {
            user = tokenService.validateBidderRegistrationTokenAndGetUser(token);
            isInitiating = true;
        } else {
            user = tokenService.validateForgottenPasswordTokenAndGetUser(token);
        }
        user.setPassword(authService.hidePassword(password));
        String companyName = userService.updateForgottenPassword(user, isInitiating);
        if (isInitiating) {
            // TODO: handle sendInitiatedPasswordMail
            // emailService.sendInitiatedPasswordMail(user, companyName);
        } else {
            // TODO: handle sendChangedPasswordMail
            // emailService.sendChangedPasswordMail(user);
        }
        return userService.generateForgottenPasswordSession(user);
    }
}

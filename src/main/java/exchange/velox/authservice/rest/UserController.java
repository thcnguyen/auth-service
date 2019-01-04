package exchange.velox.authservice.rest;

import exchange.velox.authservice.config.AuthConfig;
import exchange.velox.authservice.domain.PasswordToken;
import exchange.velox.authservice.dto.Permission;
import exchange.velox.authservice.dto.UserDTO;
import exchange.velox.authservice.dto.UserRole;
import exchange.velox.authservice.dto.UserSessionDTO;
import exchange.velox.authservice.mvc.TokenExpiredException;
import exchange.velox.authservice.mvc.UserDisabledException;
import exchange.velox.authservice.service.NotificationService;
import exchange.velox.authservice.service.TokenService;
import exchange.velox.authservice.service.UserService;
import io.swagger.annotations.*;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.utils.HandledHttpException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private Logger log = LogManager.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    @ApiOperation(value = "Login")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Successfully login"),
                @ApiResponse(code = 401, message = "You are not authorized to login"),
                @ApiResponse(code = 404, message = "User is not found")
    })
    @ApiImplicitParams({
                @ApiImplicitParam(name = "Authorization", paramType = "header"),
                @ApiImplicitParam(name = "User-Agent", paramType = "header")
    })
    public UserSessionDTO login(@RequestHeader("Authorization") String authorization,
                                @RequestHeader("User-Agent") String userAgent) {
        if (!authorization.split("_", 2)[0].equals(AuthConfig.AUTHENTICATION_SIGN)) {
            throw new HandledHttpException().statusCode(HttpStatus.UNAUTHORIZED).errorCode("AUTH_ERROR")
                        .message("Header with invalid realm");
        }
        String[] authParts = authorization.split("_", 2)[1].split(":");
        String email = authParts[0];
        String passMd5 = authParts[1]; // this password is built by: System.out.println(new String(Base64.getEncoder().encode(DigestUtils.md5("PLAIN PASSWORD")), "UTF-8"));
        UserDTO user = userService.findUserByEmail(email);
        if (user == null)
            throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND).errorCode("NOT_FOUND_USER_WITH_EMAIL")
                        .message("Cannot find user with email " + email).property("EMAIL", email);
        if (user.getPassword() == null) {
            throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND).errorCode("ACCOUNT_IS_NOT_INITIATED")
                        .message("Your account is not initiated");
        }

        if (!authService.verifyPassword(user.getPassword(), passMd5)) {
            int remainingLoginAttempts = userService.handleUserMaxLogin(user.getId());
            if (remainingLoginAttempts < 0) {
                notificationService.sendMaxLoginAttemptReached(user);
                notificationService.sendUserLockedMessage(user);
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
            } else if (UserRole.SELLER.name().equals(user.getRole()) || UserRole.BIDDER.name().equals(user.getRole())) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("DISABLED")
                            .message("User disabled");
            } else {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("NOTVALIDATED")
                            .message("User not validated");
            }
        }

        if (UserRole.SELLER.name().equals(user.getRole())) {
            if (!userService.isUserApproved(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("SELLER_NOT_VALIDATED")
                            .message("Seller not validated");
            }
            if (!userService.isUserCompanyActive(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("SELLER_DISABLED")
                            .message("Seller disabled");
            }
        }
        if (UserRole.BIDDER.name().equals(user.getRole())) {
            if (!userService.isUserApproved(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("BIDDER_NOT_VALIDATED")
                            .message("Bidder not validated");
            }
            if (!userService.isUserCompanyActive(user)) {
                throw new HandledHttpException().statusCode(HttpStatus.FORBIDDEN).errorCode("BIDDER_DISABLED")
                            .message("Bidder disabled");
            }
        }

        return userService.updateUserAndGenerateSession(user, userAgent);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.DELETE)
    @ApiOperation(value = "Logout")
    @ApiResponses(value = {
                @ApiResponse(code = 204, message = "Logout successfully")
    })
    @ApiImplicitParams({
                @ApiImplicitParam(name = "Authorization", paramType = "header")
    })
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorization) {
        userService.logout(authorization, false);
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
        UserSessionDTO sessionDTO;
        try {
            sessionDTO = userService.checkValidToken(authorization);
        } catch (TokenExpiredException | UserDisabledException e) {
            userService.logout(authorization, true);
            throw e;
        } catch (HandledHttpException e) {
            throw e;
        }
        userService.extendToken(authorization);
        return sessionDTO;
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
            throw new HandledHttpException().statusCode(500).errorCode("BODY_REQUEST_DOES_NOT_CONTAIN_EMAIL")
                        .message("Body request does not contain 'email'");
        }
        String email = data.get("email");
        UserDTO user = userService.findUserByEmail(email);
        if (user == null) {
            log.info("User for email {} not found!", email);
            return;
        }
        if (!userService.isUserApproved(user)) {
            log.info("We do not allow to forgot password for WAITING user: {}", email);
            throw new HandledHttpException().statusCode(500).errorCode("PLEASE_CONTINUE_THE_REGISTRATION")
                        .message("Please continue the registration process");
        }
        if (!userService.isUserInitiated(user)) {
            log.info("We do not allow to forgot password for WAITING user: {}", email);
            throw new HandledHttpException().statusCode(500).errorCode("USER_HAS_NOT_BEEN_INITIATED")
                        .message("This user has not been initiated, please continue the registration process");
        }
        if (BooleanUtils.isFalse(user.isActive())) {
            log.info("We do not allow to forgot password for BANNED user: {}", email);
            throw new HandledHttpException().statusCode(500).errorCode("USER_HAS_BEEN_BANNED")
                        .message("This user has been banned, please contact administrator for more information");
        }
        PasswordToken pwdToken = userService.generateForgottenPasswordToken(email);
        notificationService.sendForgotPasswordMail(user, pwdToken.getToken());
    }

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.GET)
    @ApiOperation(value = "Validate forgotten password token")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "The access token is valid"),
                @ApiResponse(code = 401, message = "The access token expired")
    })
    Map<String, String> validateForgottenPasswordToken(@ApiParam(value = "Token", required = true)
                                                       @RequestParam(value = "token") String token) {
        Map<String, String> result = new HashMap<>();
        tokenService.validateForgottenPasswordTokenAndGetUser(token);
        result.put("result", "OK");
        return result;
    }

    @RequestMapping(value = "/token/initiate", method = RequestMethod.GET)
    @ApiOperation(value = "Validate validate initiate token")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "The access token is valid"),
                @ApiResponse(code = 401, message = "The access token expired")
    })
    Map<String, String> validateInitiateToken(
                @ApiParam(value = "Token", required = true) @RequestParam(value = "token") String token,
                @ApiParam(value = "Role", required = true) @RequestParam(value = "role") String role) {
        Map<String, String> result = new HashMap<>();
        UserDTO user;
        result.put("result", "OK");
        if (UserRole.SELLER.name().equalsIgnoreCase(role)) {
            user = tokenService.validateSellerRegistrationTokenAndGetUser(token);
        } else if (UserRole.BIDDER.name().equalsIgnoreCase(role)) {
            user = tokenService.validateBidderRegistrationTokenAndGetUser(token);
        } else {
            return result;
        }
        result.put("companyName", userService.getUserCompanyName(user));

        return result;
    }

    @RequestMapping(value = "/token/inviteUser", method = RequestMethod.POST)
    @ApiOperation(value = "Invite seller user and bidder user")
    @ApiResponses(value = {
                @ApiResponse(code = 204, message = "Successfully invite user"),
                @ApiResponse(code = 401, message = "You are not authorized")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiImplicitParams({
                @ApiImplicitParam(name = "Authorization", paramType = "header")
    })
    public void inviteUser(@RequestHeader("Authorization") String authorization,
                           @RequestBody Map<String, String> data) {
        UserSessionDTO session = checkValidToken(authorization);
        UserDTO currentUser = userService.findUserByEmail(session.getEmail());
        String userType = data.get("userType");
        String email = data.get("email");
        String lang = data.get("lang");
        String firstname = data.get("firstname");
        String lastname = data.get("lastname");
        String role = data.get("role");
        String companyName = data.get("companyName");
        PasswordToken token = null;
        if (StringUtils.isAnyBlank(userType, email, lang, firstname, lastname, role, companyName)) {
            throw new HandledHttpException().statusCode(HttpStatus.BAD_REQUEST);
        }
        // prepare email dto
        UserDTO invitedUser = new UserDTO();
        invitedUser.setEmail(email);
        invitedUser.setLang(lang);
        invitedUser.setFirstname(firstname);
        invitedUser.setLastname(lastname);
        invitedUser.setRole(role);

        if (!session.getPermissions().contains(Permission.INVITE_USER.name())) {
            throw new HandledHttpException().statusCode(HttpStatus.NOT_ACCEPTABLE).errorCode("OP_DENIED")
                        .message("Not enough permission");
        }
        if (UserRole.SELLER.name().equals(userType)) {
            token = userService.generateInviteSellerUserPasswordToken(email);
        }
        if (UserRole.BIDDER.name().equals(userType)) {
            token = userService.generateInviteBidderUserPasswordToken(email);
        }
        if (token == null) {
            throw new HandledHttpException().statusCode(HttpStatus.BAD_REQUEST);
        }
        notificationService.sendMemberRegistrationMail(invitedUser, token.getToken(), companyName, currentUser);
    }

    @RequestMapping(value = "/token/forgotPassword", method = RequestMethod.PUT)
    @ApiOperation(value = "Update forgotten password")
    @ApiResponses(value = {
                @ApiResponse(code = 200, message = "Successfully update forgotten password"),
                @ApiResponse(code = 401, message = "You are not authorized"),
                @ApiResponse(code = 400, message = "Missing parameter 'password'")
    })
    public ResponseEntity updateForgottenPassword(
                @ApiParam(value = "Data model need to be update forgot password", required = true) @RequestBody Map<String, String> data) {
        String password = data.get("password");
        if (StringUtils.isEmpty(password)) {
            log.info("Missing parameter 'password'");
            throw new HandledHttpException().statusCode(HttpStatus.BAD_REQUEST)
                        .errorCode("BODY_REQUEST_MISSING_PASSWORD").message("Missing parameter 'password'");
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
        if (BooleanUtils.isFalse(user.isActive()) && StringUtils.isNotBlank(user.getPassword())) {
            log.info("We do not allow to update forgotten password for BANNED user: {}", user.getEmail());
            throw new HandledHttpException().statusCode(500).errorCode("USER_HAS_BEEN_BANNED")
                        .message("This user has been banned, please contact administrator for more information");
        }
        user.setPassword(authService.hidePassword(password));
        String companyName = userService.updateForgottenPassword(user, isInitiating);
        if (isInitiating) {
            notificationService.sendInitiatedPasswordMail(user, companyName);
        } else {
            notificationService.sendChangedPasswordMail(user);
            userService.removeAllSavedLoginSessions(user);
        }

        Map<String, String> result = new HashMap<>();
        result.put("result", "OK");
        return ResponseEntity.ok(result);
    }
}

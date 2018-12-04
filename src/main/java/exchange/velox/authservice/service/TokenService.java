package exchange.velox.authservice.service;

import exchange.velox.authservice.dao.PasswordTokenDAO;
import exchange.velox.authservice.dao.UserDAO;
import exchange.velox.authservice.domain.PasswordToken;
import exchange.velox.authservice.domain.TokenType;
import exchange.velox.authservice.domain.UserDTO;
import net.etalia.crepuscolo.utils.HandledHttpException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.security.SecureRandom;

public class TokenService {

    private long forgotPasswordExpiryInMilliseconds;
    private long sellerUserRegistrationInMinutes;
    private long bidderUserRegistrationInMinutes;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private PasswordTokenDAO passwordTokenDAO;

    public UserDTO validateForgottenPasswordTokenAndGetUser(String token) {
        return validateTokenAndGetUser(TokenType.FORGOT_PWD, token, forgotPasswordExpiryInMilliseconds);
    }

    public UserDTO validateSellerRegistrationTokenAndGetUser(String token) {
        return validateTokenAndGetUser(TokenType.SELLER_USER_INVITE, token, sellerUserRegistrationInMinutes);
    }

    public UserDTO validateBidderRegistrationTokenAndGetUser(String token) {
        return validateTokenAndGetUser(TokenType.BIDDER_USER_INVITE, token, bidderUserRegistrationInMinutes);
    }

    private UserDTO validateTokenAndGetUser(TokenType tokenType, String token, long expireTime) {
        if (StringUtils.isBlank(token)) {
            missingToken();
            return null;
        }
        PasswordToken pwdToken = passwordTokenDAO.findPasswordTokenByToken(token);
        if (pwdToken == null) {
            invalidToken();
        }
        if (StringUtils.isBlank(pwdToken.getEmail()) || pwdToken.getTimestamp() == null || !tokenType
                    .equals(pwdToken.getTokenType())) {
            invalidToken();
        }
        if ((System.currentTimeMillis() - pwdToken.getTimestamp()) > expireTime) {
            expiredToken();
        }
        UserDTO user = userDAO.findUserByEmail(pwdToken.getEmail());
        if (user == null) {
            userNotFound();
        }
        return user;
    }

    public void invalidToken() {
        throw new HandledHttpException().statusCode(
                    HttpStatus.BAD_REQUEST).errorCode("INVALID_TOKEN").message("Token is invalid");
    }

    public void expiredToken() {
        throw new HandledHttpException().statusCode(HttpStatus.BAD_REQUEST).errorCode("EXPIRED_TOKEN")
                    .message("Token has expired");
    }

    public void missingToken() {
        throw new HandledHttpException().statusCode(HttpStatus.BAD_REQUEST).errorCode("MISSING_TOKEN")
                    .message("Token is missing");
    }

    public void auctionNotFound() {
        throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND).errorCode("NOT_FOUND")
                    .message("Auction is not found");
    }

    public void userAlreadyVerified() {
        throw new HandledHttpException().statusCode(HttpStatus.PRECONDITION_FAILED).errorCode("ALREADY_VERIFIED")
                    .message("User already verified");
    }

    public void userAlreadyInitiated() {
        throw new HandledHttpException().statusCode(HttpStatus.PRECONDITION_FAILED).errorCode("ALREADY_INITIATED")
                    .message("User already initiated");
    }

    public void userNotFound() {
        throw new HandledHttpException().statusCode(HttpStatus.NOT_FOUND).errorCode("NOT_FOUND")
                    .message("User is not found");
    }

    public String generateRandomToken() {
        SecureRandom sr = new SecureRandom();
        sr.setSeed(sr.generateSeed(16));
        byte[] result = new byte[100];
        sr.nextBytes(result);
        return org.apache.commons.codec.binary.Hex.encodeHexString(result);
    }

    public long getForgotPasswordExpiryInMilliseconds() {
        return forgotPasswordExpiryInMilliseconds;
    }

    public void setForgotPasswordExpiryInMilliseconds(long forgotPasswordExpiryInMilliseconds) {
        this.forgotPasswordExpiryInMilliseconds = forgotPasswordExpiryInMilliseconds;
    }

    public long getSellerUserRegistrationInMinutes() {
        return sellerUserRegistrationInMinutes;
    }

    public void setSellerUserRegistrationInMinutes(long sellerUserRegistrationInMinutes) {
        this.sellerUserRegistrationInMinutes = sellerUserRegistrationInMinutes;
    }

    public long getBidderUserRegistrationInMinutes() {
        return bidderUserRegistrationInMinutes;
    }

    public void setBidderUserRegistrationInMinutes(long bidderUserRegistrationInMinutes) {
        this.bidderUserRegistrationInMinutes = bidderUserRegistrationInMinutes;
    }
}

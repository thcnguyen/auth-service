package exchange.velox.authservice.config;

import exchange.velox.authservice.service.TokenService;
import exchange.velox.authservice.service.UtilsService;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.auth.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class Config extends WebMvcConfigurerAdapter {

    protected ApplicationContext appctx;

    @Autowired
    public Config(ApplicationContext appctx) {
        this.appctx = appctx;
    }

    @Bean
    public AuthService authService(@Value("${auth.salt}") String authSalt,
                                   @Value("${auth.sign}") String authSign,
                                   @Value("${auth.sessionTime:24*60*60000}") Long sessionTime) {
        AuthServiceImpl authService = new AuthServiceImpl();
        AuthConfig.MAX_SESSION_TIME = sessionTime;
        AuthConfig.STATIC_SALT = authSalt;
        AuthConfig.AUTHENTICATION_SIGN = authSign;
        return authService;
    }

    @Bean
    public TokenService tokenService(@Value("${token.forgot-password.expired-in-minutes:60}") long forgotPasswordInMinutes,
                                     @Value("${token.seller-user-registration.expired-in-minutes:4320}" /* 3 days */) long sellerUserRegistrationInMinutes,
                                     @Value("${token.bidder-user-registration.expired-in-minutes:4320}" /* 3 days */) long bidderUserRegistrationInMinutes) {

        TokenService tokenService = new TokenService();
        tokenService.setForgotPasswordExpiryInMilliseconds(forgotPasswordInMinutes * 60_000);
        tokenService.setSellerUserRegistrationInMinutes(sellerUserRegistrationInMinutes * 60_000);
        tokenService.setBidderUserRegistrationInMinutes(bidderUserRegistrationInMinutes * 60_000);
        return tokenService;
    }

    @Bean
    public UtilsService utilsService() {
        return new UtilsService();
    }
}

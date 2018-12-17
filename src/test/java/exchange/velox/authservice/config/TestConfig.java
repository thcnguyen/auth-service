package exchange.velox.authservice.config;

import exchange.velox.authservice.mvc.JsonHttpExceptionHandler;
import exchange.velox.authservice.mvc.RequestLoggerFilter;
import exchange.velox.authservice.service.TokenService;
import exchange.velox.authservice.service.UtilsService;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.auth.AuthServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.HandlerExceptionResolver;

@TestConfiguration
@Profile("test")
public class TestConfig extends Config {

    private ApplicationContext appctx;

    @Autowired
    public TestConfig(ApplicationContext appctx) {
        super(appctx);
        this.appctx = appctx;
    }

    @Bean
    @Override
    public AuthService authService(@Value("${auth.salt}") String authSalt,
                                   @Value("${auth.sign}") String authSign,
                                   @Value("${auth.sessionTime:24*60*60000}") Long sessionTime) {
        AuthServiceImpl authService = new AuthServiceImpl();
        AuthConfig.MAX_SESSION_TIME = sessionTime;
        AuthConfig.STATIC_SALT = authSalt;
        AuthConfig.AUTHENTICATION_SIGN = authSign;
        return authService;
    }

    @Override
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

    @Override
    @Bean
    public UtilsService utilsService() {
        return new UtilsService();
    }

    @Override
    @Bean
    public FilterRegistrationBean<RequestLoggerFilter> requestLoggerFilter() {
        FilterRegistrationBean<RequestLoggerFilter> frb = new FilterRegistrationBean<>();;
        frb.setFilter(new RequestLoggerFilter());
        frb.addUrlPatterns("/*");
        frb.setName("RequestLoggerFilter");
        return frb;
    }

    @Override
    @Bean
    public HandlerExceptionResolver jsonHttpExceptionHandler() {
        return new JsonHttpExceptionHandler();
    }
}

package exchange.velox.authservice.config;

import exchange.velox.authservice.gateway.DocgenServiceGateway;
import exchange.velox.authservice.gateway.EmailServiceGateway;
import exchange.velox.authservice.gateway.impl.DocgenServiceGatewayImpl;
import exchange.velox.authservice.gateway.impl.EmailServiceGatewayImpl;
import exchange.velox.authservice.mvc.JsonHttpExceptionHandler;
import exchange.velox.authservice.mvc.RequestLoggerFilter;
import exchange.velox.authservice.service.EmailService;
import exchange.velox.authservice.service.TokenService;
import exchange.velox.authservice.service.UtilsService;
import net.etalia.crepuscolo.auth.AuthService;
import net.etalia.crepuscolo.auth.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Config implements WebMvcConfigurer {

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
    public TokenService tokenService(
                @Value("${token.forgot-password.expired-in-minutes:60}") long forgotPasswordInMinutes,
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

    @Bean
    public FilterRegistrationBean<RequestLoggerFilter> requestLoggerFilter() {
        FilterRegistrationBean<RequestLoggerFilter> frb = new FilterRegistrationBean<>();
        frb.setFilter(new RequestLoggerFilter());
        frb.addUrlPatterns("/*");
        frb.setName("RequestLoggerFilter");
        return frb;
    }

    @Bean(name = "veloRest")
    public RestTemplate veloRest() {
        return new RestTemplate();
    }

    @Bean
    public HandlerExceptionResolver jsonHttpExceptionHandler() {
        return new JsonHttpExceptionHandler();
    }

    @Bean
    public DocgenServiceGateway docgenServiceGateway() {
        return new DocgenServiceGatewayImpl();
    }

    @Bean
    public EmailServiceGateway emailServiceGateway() {
        return new EmailServiceGatewayImpl();
    }

    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setBasenames("i18n/locale");
        return messageSource;
    }

    @Bean
    public EmailService emailService(@Value("${mail.from}") String from,
                                     @Value("${server.web}") String serverWeb) {
        EmailService emailService = new EmailService();
        emailService.setFrom(from);
        emailService.setServerWeb(serverWeb);
        return emailService;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("origin", "accept", "authorization", "content-type", "x-token", "x-2fa-token")
                    .exposedHeaders("x-authorization");
    }
}

package exchange.velox.authservice.authservice.config;

import exchange.velox.authservice.authservice.service.UtilsService;
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
                                   @Value("${auth.tokenTime}") Long tokenTime) {
        AuthServiceImpl authService = new AuthServiceImpl();
        authService.setMaxTokenTime(tokenTime);
        AuthConfig.STATIC_SALT = authSalt;
        AuthConfig.AUTHENTICATION_SIGN = authSign;
        return authService;
    }

    @Bean
    public UtilsService utilsService() {
        return new UtilsService();
    }
}

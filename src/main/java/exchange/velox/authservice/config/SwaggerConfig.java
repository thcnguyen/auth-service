package exchange.velox.authservice.config;

import exchange.velox.commonutils.GitUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestHeader;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static Logger log = LogManager.getLogger(SwaggerConfig.class);

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(RequestHeader.class).select()
                    .apis(RequestHandlerSelectors.basePackage("exchange.velox.authservice.rest"))
                    .paths(PathSelectors.any()).build()
                    .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                    "Auth REST API",
                    "Micro service for authentication and authorization",
                    GitUtils.getAppVersion(),
                    null,
                    null,
                    null, null, Collections.emptyList());
    }
}
package exchange.velox.authservice.config;

import exchange.velox.authservice.util.JsonUtils;
import org.apache.commons.io.IOUtils;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

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
                    getAppVersion(),
                    null,
                    null,
                    null, null, Collections.emptyList());
    }

    private String getAppVersion() {
        try {
            try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("git.properties")) {
                String gitString = IOUtils.toString(inputStream, "UTF-8");
                Map<String, Object> gitInfo = JsonUtils.readToMap(gitString);
                return String.valueOf(gitInfo.get("git.build.version")) + "-" + String
                            .valueOf(gitInfo.get("git.commit.id.abbrev"));
            }
        } catch (Exception e ) {
            log.warn("Failed to read git.properties from classpath", e);
            return "Version information could not be retrieved";
        }
    }
}
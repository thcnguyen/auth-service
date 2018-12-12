package exchange.velox.authservice.gateway;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public interface DocgenServiceGateway {
    default String generateEmailContent(String name, String language, Map<String, Object> model) {
        return StringUtils.EMPTY;
    }
}

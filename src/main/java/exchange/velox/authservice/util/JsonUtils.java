package exchange.velox.authservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.velox.authservice.config.SwaggerConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;

public class JsonUtils {
    private static Logger log = LogManager.getLogger(SwaggerConfig.class);
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static Map<String, Object> readToMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not parse the input json to map", e);
        }
    }
}

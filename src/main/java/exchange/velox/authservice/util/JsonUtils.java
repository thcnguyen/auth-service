package exchange.velox.authservice.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import exchange.velox.authservice.config.SwaggerConfig;
import exchange.velox.authservice.dto.EmailOptionDTO;
import exchange.velox.authservice.util.view.Views;
import org.apache.commons.lang3.StringUtils;
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

    public static String getLogView(EmailOptionDTO object) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
        try {
            return mapper.writerWithView(Views.LogView.class).writeValueAsString(object);
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }
}

package exchange.velox.authservice.gateway.impl;

import exchange.velox.authservice.gateway.DocgenServiceGateway;
import exchange.velox.authservice.util.VeloService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DocgenServiceGatewayImpl implements DocgenServiceGateway {

    private static Log log = LogFactory.getLog(DocgenServiceGatewayImpl.class);

    @Autowired
    protected RestTemplate veloRest;

    @Override
    public String generateEmailContent(String template, String language, Map<String, Object> modelInput) {

        try {
            byte[] base64 = generate(template, language, modelInput, false);
            return new String(base64, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cannot parse response payload", e);
        }
    }

    private byte[] generate(String template, String language, Map<String, Object> modelInput, boolean safeToFail) {

        Map<String, Object> model = new HashMap<>(modelInput);
        if (StringUtils.isBlank(language)) {
            language = autoDetectLanguage(model);
        }

        String url = VeloService.docgen().rest().generate()
                    .p("template", template)
                    .p("language", language)
                    .p("silent", String.valueOf(safeToFail))
                    .build();

        final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(model);
        ResponseEntity<Map> response = veloRest.exchange(url, HttpMethod.POST, entity, Map.class);
        Map responseBody = response.getBody();
        String base64 = String.valueOf(responseBody.get("base64"));

        Object error = responseBody.get("error");
        if (error != null && error instanceof Map) {
            Map<String, Object> errorMap = (Map<String, Object>) error;
            log.error(String.valueOf(errorMap.get("errorMessage")));
        }

        return Base64.getDecoder().decode(base64);
    }

    private String autoDetectLanguage(Map<String, Object> model) {
        Object locale = model.get("locale");
        if (locale == null) {
            return StringUtils.EMPTY;
        }
        return ((Locale) locale).getLanguage();
    }
}

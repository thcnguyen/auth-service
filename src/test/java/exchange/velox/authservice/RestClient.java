package exchange.velox.authservice;

import exchange.velox.authservice.dto.UserSessionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestClient {
    private final RestTemplate restTemplate;

    @Value("${server.port:8800}")
    private int port;
    private static final String LOCAL_URL = "http://localhost:%s/auth/";
    private static final String LOGIN_URL = "login";
    private static final String TOKEN_URL = "token";
    private static final String LOGOUT_URL = "logout";

    public RestClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public UserSessionDTO login(String authorization) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(getLocalUrl() + LOGIN_URL, HttpMethod.GET, entity, UserSessionDTO.class).getBody();
    }

    public UserSessionDTO token(String authorization) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(getLocalUrl() + TOKEN_URL, HttpMethod.GET, entity, UserSessionDTO.class).getBody();
    }

    public void logout(String authorization) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        restTemplate.exchange(getLocalUrl() + LOGOUT_URL, HttpMethod.DELETE, entity, UserSessionDTO.class).getBody();
    }

    private String getLocalUrl() {
        return String.format(LOCAL_URL, port);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

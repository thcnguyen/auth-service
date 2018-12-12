package exchange.velox.authservice;

import exchange.velox.authservice.dto.UserSessionDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestClient {
    private final RestTemplate restTemplate;
    private static final String LOCAL_URL = "http://localhost:8800/auth/";
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
        return restTemplate.exchange(LOCAL_URL + LOGIN_URL, HttpMethod.GET, entity, UserSessionDTO.class).getBody();
    }

    public UserSessionDTO token(String authorization) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(LOCAL_URL + TOKEN_URL, HttpMethod.GET, entity, UserSessionDTO.class).getBody();
    }

    public void logout(String authorization) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorization);
        final HttpEntity<String> entity = new HttpEntity<>(headers);
        restTemplate.exchange(LOCAL_URL + LOGOUT_URL, HttpMethod.GET, entity, UserSessionDTO.class).getBody();
    }

}

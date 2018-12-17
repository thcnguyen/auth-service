package exchange.velox.authservice.gateway;

import exchange.velox.authservice.dto.EmailRequestDTO;

import java.util.Map;

public interface NotificationServiceGateway {

    void sendMail(EmailRequestDTO emailRequestDTO);

    void postToTeams(String template, Map<String, Object> data);
}

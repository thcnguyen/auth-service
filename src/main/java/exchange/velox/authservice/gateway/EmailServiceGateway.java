package exchange.velox.authservice.gateway;

import exchange.velox.authservice.dto.EmailRequestDTO;

public interface EmailServiceGateway {

    void sendMail(EmailRequestDTO emailRequestDTO);
}

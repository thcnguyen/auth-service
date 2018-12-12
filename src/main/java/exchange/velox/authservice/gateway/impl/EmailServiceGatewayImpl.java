package exchange.velox.authservice.gateway.impl;

import exchange.velox.authservice.dto.EmailRequestDTO;
import exchange.velox.authservice.gateway.EmailServiceGateway;
import exchange.velox.authservice.util.VeloService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

public class EmailServiceGatewayImpl implements EmailServiceGateway {

    private Log log = LogFactory.getLog(EmailServiceGatewayImpl.class);

    @Autowired
    private RestTemplate veloRest;

    @Override
    public void sendMail(EmailRequestDTO emailRequestDTO) {
        String url = VeloService.notification().sendMailRequest().build();
        final HttpEntity<EmailRequestDTO> entity = new HttpEntity<>(emailRequestDTO);
        log.info("Request data " + emailRequestDTO.toString());
        try {
            ResponseEntity<Map> response = veloRest.exchange(url, HttpMethod.POST, entity, Map.class);
            log.info("Response from email service: " + response.getStatusCode());
        } catch (Exception e) {
            log.warn("Exception when calling log-service " + e.getMessage());
        }
    }
}

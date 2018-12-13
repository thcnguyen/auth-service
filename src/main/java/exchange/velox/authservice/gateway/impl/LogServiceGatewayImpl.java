package exchange.velox.authservice.gateway.impl;

import exchange.velox.authservice.dto.MasterLogDTO;
import exchange.velox.authservice.gateway.LogServiceGateway;
import exchange.velox.authservice.util.VeloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LogServiceGatewayImpl implements LogServiceGateway {

    private Logger log = LoggerFactory.getLogger(LogServiceGatewayImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void addLog(MasterLogDTO masterLogDTO) {
        log.debug("Service push log {}", masterLogDTO);
        String url = VeloService.log().add().build();
        HttpEntity<MasterLogDTO> request = new HttpEntity<>(masterLogDTO);
        try {
            ResponseEntity<MasterLogDTO> response = restTemplate
                        .exchange(url, HttpMethod.POST, request, MasterLogDTO.class);
            log.debug("Response from log server {}", response.getStatusCode());
        } catch (Exception e) {
            log.warn("Exception when calling log-service {}", e.getMessage());
        }
    }
}

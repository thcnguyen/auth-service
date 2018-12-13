package exchange.velox.authservice.service;

import exchange.velox.authservice.dto.MasterLogDTO;
import exchange.velox.authservice.dto.UserDTO;
import exchange.velox.authservice.dto.VeloxEventType;
import exchange.velox.authservice.gateway.LogServiceGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    @Autowired
    private LogServiceGateway logServiceGateway;

    public void addLog(UserDTO author, String logData) {
        MasterLogDTO masterLogDTO =
                    new MasterLogDTO.MasterLogBuilder(VeloxEventType.SEND_EMAIL)
                                .withAuthor(author)
                                .logData(logData)
                                .build();
        logServiceGateway.addLog(masterLogDTO);
    }
}

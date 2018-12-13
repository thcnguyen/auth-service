package exchange.velox.authservice.gateway;


import exchange.velox.authservice.dto.MasterLogDTO;

public interface LogServiceGateway {

    /**
     * Push data log to log service
     * 
     * @param masterLogDTO
     */
    void addLog(MasterLogDTO masterLogDTO);
}

package exchange.velox.authservice.dao;

import exchange.velox.authservice.domain.PasswordToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordTokenDAO extends CrudRepository<PasswordToken, Long> {
    PasswordToken findPasswordTokenByToken(String token);
}

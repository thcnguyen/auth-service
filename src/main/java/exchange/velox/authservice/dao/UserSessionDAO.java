package exchange.velox.authservice.dao;

import exchange.velox.authservice.domain.UserSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionDAO extends CrudRepository<UserSession, Long> {
    Optional<UserSession> findUserSessionByUserId(String userId);

    Optional<List<UserSession>> findUserSessionsByUserId(String userId);

    Optional<UserSession> findUserSessionByToken(String token);
}

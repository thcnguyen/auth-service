package exchange.velox.authservice.authservice.dao;

import exchange.velox.authservice.authservice.domain.UserSession;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionDAO extends CrudRepository<UserSession, Long> {
    Optional<UserSession> findUserSessionByUid(String uid);

    Optional<List<UserSession>> findUserSessionsByUid(String uid);

    Optional<UserSession> findUserSessionByToken(String token);
}

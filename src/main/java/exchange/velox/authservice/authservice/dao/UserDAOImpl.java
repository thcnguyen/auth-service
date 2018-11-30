package exchange.velox.authservice.authservice.dao;

import exchange.velox.authservice.authservice.domain.UserDTO;
import exchange.velox.authservice.authservice.domain.UserRole;
import exchange.velox.authservice.authservice.service.UtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserDAOImpl implements UserDAO {

    @Autowired
    UtilsService utilsService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserDTO load(String id) {
        Query query = entityManager.createNativeQuery(
                    "select id, email, password, role, active, lastLogin, loginAttempt from user where id = ?1");
        query.setParameter(1, id);
        try {
            Object[] result = (Object[]) query.getSingleResult();
            return utilsService.mapToUserDTO(result);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UserDTO findUserByEmail(String email) {
        Query query = entityManager.createNativeQuery(
                    "select id, email, password, role, active, lastLogin, loginAttempt from user where email = ?1");
        query.setParameter(1, email);
        try {
            Object[] result = (Object[]) query.getSingleResult();
            return utilsService.mapToUserDTO(result);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        Query query = entityManager.createNativeQuery(
                    "update user set active = ?1, lastLogin = ?2, loginAttempt = ?3 where id = ?4");
        query.setParameter(1, userDTO.getActive());
        query.setParameter(2, userDTO.getLastLogin());
        query.setParameter(3, userDTO.getLoginAttempt());
        query.setParameter(4, userDTO.getId());
        try {
            query.executeUpdate();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean getCompanyStatusByUser(UserDTO userDTO) {
        StringBuilder queryBuilder = new StringBuilder();
        boolean isActive = false;
        if (userDTO.getRole().equals(UserRole.SELLER.name())) {
            queryBuilder.append("select u.active from user u " +
                                            "inner join sellerUser su on su.seller_Id = u.id " +
                                            "and su.id = ?1");
        } else if (userDTO.getRole().equals(UserRole.BIDDER.name())) {
            queryBuilder.append("select u.active from user u " +
                                            "inner join bidderUser bu on bu.bidder_Id = u.id " +
                                            "and bu.id = ?1");
        } else {
            return true;
        }
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, userDTO.getId());
        try {
            isActive = (boolean) query.getSingleResult();
        } catch (Exception e) {
            // ignore
        }
        return isActive;
    }

    @Override
    public Set<String> getPermissionListByUser(UserDTO userDTO) {
        Set<String> result = new HashSet<>();
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select permission from ");
        if (userDTO.getRole().equals(UserRole.SELLER.name())) {
            queryBuilder.append("sellerPermission where sellerUser_Id = ?1");
        } else if (userDTO.getRole().equals(UserRole.BIDDER.name())) {
            queryBuilder.append("bidderPermission where bidderUser_Id = ?1");
        } else {
            return result;
        }
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, userDTO.getId());
        try {
            result = (Set<String>) query.getResultStream().collect(Collectors.toSet());
        } catch (Exception e) {
            // ignore
        }
        return result;
    }
}

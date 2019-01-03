package exchange.velox.authservice.dao;

import exchange.velox.authservice.dto.UserDTO;
import exchange.velox.authservice.dto.UserRole;
import exchange.velox.authservice.service.UtilsService;
import org.apache.commons.lang3.StringUtils;
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
                    "select id, email, password, role, active, lastLogin, loginAttempt, lang, totpRequiredAtLogin from user where id = ?1");
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
                    "select id, email, password, role, active, lastLogin, loginAttempt, lang, totpRequiredAtLogin from user where email = ?1");
        query.setParameter(1, email);
        try {
            Object[] result = (Object[]) query.getSingleResult();
            return utilsService.mapToUserDTO(result);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public UserDTO enrichUserInfo(UserDTO userDTO) {
        StringBuilder queryBuilder = new StringBuilder();
        if (userDTO.getRole().equals(UserRole.SELLER.name())) {
            queryBuilder.append("select su.firstname,su.lastname,su.searchableId from seller s " +
                                            "inner join sellerUser su on s.id = su.seller_Id where su.id =?1");
        } else if (userDTO.getRole().equals(UserRole.BIDDER.name())) {
            queryBuilder.append("select bu.firstname,bu.lastname,bu.searchableId from bidder b " +
                                            "inner join bidderUser bu on b.id = bu.bidder_Id where bu.id =?1");
        } else if (userDTO.getRole().equals(UserRole.INTRODUCER.name())) {
            queryBuilder.append("select firstname,lastname,searchableId from introducer where id  =?1");
        } else if (userDTO.getRole().equals(UserRole.CREDIT_ANALYST.name())) {
            queryBuilder.append("select firstname, lastname, cast(null as char) from creditAnalyst where id = ?1");
        } else if (userDTO.getRole().equals(UserRole.DATA_ENTRY.name())) {
            queryBuilder.append("select firstname, lastname, cast(null as char) from dataEntry where id = ?1");
        }
        if(StringUtils.isEmpty(queryBuilder)) {
            return userDTO;
        }
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, userDTO.getId());
        try {
            Object[] result = (Object[]) query.getSingleResult();
            if (result != null && result.length > 0) {
                userDTO.setFirstname((result[0] != null) ? String.valueOf(result[0]) : null);
                userDTO.setLastname((result[1] != null) ? String.valueOf(result[1]) : null);
                userDTO.setSearchableId((result[2] != null) ? String.valueOf(result[2]) : null);
            }
        } catch (Exception e) {
            // ignore
        }
        return userDTO;
    }

    @Override
    public String getUserApprovationStep(UserDTO userDTO) {
        StringBuilder queryBuilder = new StringBuilder();
        String result = null;
        if (userDTO.getRole().equals(UserRole.SELLER.name())) {
            queryBuilder.append("select s.approvationStep from seller s " +
                                            "inner join sellerUser su on s.id = su.seller_Id where su.id =?1");
        } else if (userDTO.getRole().equals(UserRole.BIDDER.name())) {
            queryBuilder.append("select b.approvationStep from bidder b " +
                                            "inner join bidderUser bu on b.id = bu.bidder_Id where bu.id =?1");
        } else if (userDTO.getRole().equals(UserRole.INTRODUCER.name())) {
            queryBuilder.append("select approvationStep from introducer where id  =?1");
        }
        else {
            return result;
        }
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, userDTO.getId());
        try {
            result = (String) query.getSingleResult();
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    @Override
    public Object[] getUserCompanyNameAndId(UserDTO userDTO) {
        StringBuilder queryBuilder = new StringBuilder();
        Object[] result = null;
        if (userDTO.getRole().equals(UserRole.SELLER.name())) {
            queryBuilder.append("select s.companyName, s.id from seller s " +
                                            "inner join sellerUser su on s.id = su.seller_Id where su.id =?1");
        } else if (userDTO.getRole().equals(UserRole.BIDDER.name())) {
            queryBuilder.append("select b.companyName, b.id from bidder b " +
                                            "inner join bidderUser bu on b.id = bu.bidder_Id where bu.id =?1");
        } else if (userDTO.getRole().equals(UserRole.INTRODUCER.name())) {
            queryBuilder.append("select companyName, id from introducer where id  =?1");
        } else {
            return result;
        }
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, userDTO.getId());
        try {
            result = (Object[]) query.getSingleResult();
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    @Override
    public boolean updateUser(UserDTO userDTO) {
        Query query = entityManager.createNativeQuery(
                    "update user set active = ?1, lastLogin = ?2, loginAttempt = ?3, password = ?4 where id = ?5");
        query.setParameter(1, userDTO.getActive());
        query.setParameter(2, userDTO.getLastLogin());
        query.setParameter(3, userDTO.getLoginAttempt());
        query.setParameter(4, userDTO.getPassword());
        query.setParameter(5, userDTO.getId());
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

    @Override
    public boolean isUserInitiated(UserDTO userDTO) {
        boolean result = false;
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select initiated from ");
        if (userDTO.getRole().equals(UserRole.SELLER.name())) {
            queryBuilder.append("sellerUser where id = ?1");
        } else if (userDTO.getRole().equals(UserRole.BIDDER.name())) {
            queryBuilder.append("bidderUser where id = ?1");
        } else {
            return true;
        }
        Query query = entityManager.createNativeQuery(queryBuilder.toString());
        query.setParameter(1, userDTO.getId());
        try {
            result = (boolean) query.getSingleResult();
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    @Override
    public void updateInitiatedStatus(UserDTO userDTO, boolean initiated) {
        StringBuilder queryBuilder = new StringBuilder();
        if (UserRole.SELLER.name().equals(userDTO.getRole())) {
            queryBuilder.append("update sellerUser set initiated = ?1 where id =?2");
        } else if (UserRole.BIDDER.name().equals(userDTO.getRole())) {
            queryBuilder.append("update bidderUser set initiated = ?1 where id =?2");
        }
        String sql = queryBuilder.toString();
        if (StringUtils.isNotEmpty(sql)) {
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter(1, initiated);
            query.setParameter(2, userDTO.getId());
            try {
                query.executeUpdate();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}

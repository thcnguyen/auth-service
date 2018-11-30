package exchange.velox.authservice.authservice.dao;

import exchange.velox.authservice.authservice.domain.UserDTO;

import java.util.Set;

public interface UserDAO {
    UserDTO load(String id);
    UserDTO findUserByEmail(String email);
    boolean updateUser(UserDTO userDTO);
    boolean getCompanyStatusByUser(UserDTO userDTO);
    Set<String> getPermissionListByUser(UserDTO userDTO);
}

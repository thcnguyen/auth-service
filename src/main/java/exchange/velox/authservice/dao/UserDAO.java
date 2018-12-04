package exchange.velox.authservice.dao;

import exchange.velox.authservice.domain.UserDTO;

import java.util.Set;

public interface UserDAO {
    UserDTO load(String id);
    UserDTO findUserByEmail(String email);
    String getUserApprovationStep(UserDTO userDTO);
    String getUserCompanyName(UserDTO userDTO);
    boolean updateUser(UserDTO userDTO);
    boolean getCompanyStatusByUser(UserDTO userDTO);
    Set<String> getPermissionListByUser(UserDTO userDTO);
    boolean isUserInitiated(UserDTO userDTO);
    void updateInitiatedStatus(UserDTO userDTO, boolean initiated);
}

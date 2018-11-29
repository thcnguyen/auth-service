package exchange.velox.authservice.authservice.dao;

import exchange.velox.authservice.authservice.domain.UserDTO;

public interface UserDAO {
    UserDTO load(String id);
    UserDTO findUserByEmail(String email);
    boolean updateUser(UserDTO userDTO);
}

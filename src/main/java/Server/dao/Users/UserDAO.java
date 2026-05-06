package Server.dao.Users;

import Server.model.User;

import java.time.LocalDateTime;

public interface UserDAO {

    User findByUsername(String username);

    User findByUsernameAndPassword(String username, String password);

    void createUser(User user);

    void deleteUser(String username);

    boolean exists(String username);

    void createSession(int userId, String token, LocalDateTime expiresAt);

    void deleteSession(String token);

    User findUserByToken(String token);
}
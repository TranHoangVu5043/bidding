package Server.service.users;

import Server.dao.users.UserDAO;
import Server.dto.requests.UserRequestDTO;
import Server.model.users.User;
import Server.model.users.UserFactory;
import Server.model.users.records.UserRow;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean register(UserRequestDTO req) {

        if (userDAO.exists(req.getUsername())) {
            return false;
        }

        String hash = BCrypt.withDefaults().hashToString(12, req.getPassword().toCharArray());

        UserRow userRow = new UserRow(0, req.getUsername(), hash, req.getEmail(), req.getRole(), 0, req.getStoreName());
        User user = UserFactory.createUser(userRow);

        userDAO.createUser(user);

        return true;
    }


    public String login(String username, String password) {

        User user = userDAO.findByUsername(username);

        if (user == null) {
            return null;
        }

        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword().toCharArray());

        if (!result.verified) {
            return null;
        }

        // create session
        String token = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);

        userDAO.createSession(user.getId(), token, expiresAt);

        return token;
    }

    public User authenticate(String token) {
        if (token == null || token.isEmpty()) return null;
        return userDAO.findUserByToken(token);
    }

    public void logout(String token) {
        if (token != null) {
            userDAO.deleteSession(token);
        }
    }
}
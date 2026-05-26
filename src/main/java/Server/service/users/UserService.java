package Server.service.users;

import Server.dao.users.UserDAO;
import Server.dto.requests.UserRequestDTO;
import Server.model.users.User;
import Server.model.users.UserFactory;
import Server.model.users.records.UserRow;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
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

        UserRow userRow = new UserRow(0, req.getUsername(), hash, req.getEmail(), "ADMIN", 0, req.getStoreName());
        User user = UserFactory.createUser(userRow);

        userDAO.createUser(user);

        return true;
    }

    public List<User> getAllUsers() {
        return userDAO.findAll();
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

    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    public boolean changePassword(User currentUser, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isBlank()) {
            throw new IllegalArgumentException("Mật khẩu cũ không được để trống.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }

        User stored = userDAO.findById(currentUser.getId());
        if (stored == null) return false;

        BCrypt.Result verified = BCrypt.verifyer()
                .verify(oldPassword.toCharArray(), stored.getPassword().toCharArray());
        if (!verified.verified) return false;

        String newHash = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
        userDAO.updatePassword(currentUser.getId(), newHash);
        return true;
    }
}
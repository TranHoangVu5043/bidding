package Server.service.users;

import Server.dao.users.UserDAO;
import Server.dto.requests.UserRequestDTO;
import Server.model.users.User;
import Server.model.users.UserFactory;
import Server.model.users.records.UserRow;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
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

        String role = (req.getRole() != null && !req.getRole().isBlank()) ? req.getRole() : "USER";
        UserRow userRow = new UserRow(0, req.getUsername(), hash, req.getEmail(), role, 0, req.getStoreName(), "ACTIVE");
        User user = UserFactory.createUser(userRow);

        int newId = userDAO.createUser(user);
        if (newId > 0) userDAO.createDefaultSettings(newId);

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
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusHours(24);

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

    public List<User> getAllUsers() {
        return userDAO.findAll();
    }

    public void setUserStatus(int userId, String status) {
        userDAO.updateStatus(userId, status);
    }

    public void updateNotifPrefs(int userId, boolean notifAuction, boolean notifEmail) {

        userDAO.updateNotifPrefs(userId, notifAuction, notifEmail);
    }

    public void deleteAccount(int userId) {
        userDAO.deleteAllSessions(userId);

        userDAO.deleteUser(userId);
    }

    public double deposit(int userId, double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Số tiền nạp phải lớn hơn 0.");
        if (amount > 1_000_000_000) throw new IllegalArgumentException("Số tiền nạp tối đa 1.000.000.000 ₫ mỗi lần.");

        userDAO.addBalance(userId, amount);

        User updated = userDAO.findById(userId);

        return updated != null ? updated.getBalance() : 0;
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
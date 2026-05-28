package Server.service.users;

import Server.dao.users.UserDAO;
import Server.dto.requests.UserRequestDTO;
import Server.model.users.User;
import Server.model.users.UserFactory;
import Server.model.users.records.UserRow;
import at.favre.lib.crypto.bcrypt.BCrypt;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {

    private final UserDAO userDAO;

    // ── In-memory session cache ──────────────────────────────────────────────
    // Eliminates the findUserByToken() DB round-trip on every authenticated
    // request.  Entries expire after 10 minutes; logout evicts immediately.
    private static final long CACHE_TTL_MS = 10 * 60 * 1000L; // 10 min

    private record CacheEntry(User user, long cachedAt) {}

    private final Map<String, CacheEntry> sessionCache = new ConcurrentHashMap<>();

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    private void cacheToken(String token, User user) {
        sessionCache.put(token, new CacheEntry(user, System.currentTimeMillis()));
    }

    private User getCached(String token) {
        CacheEntry e = sessionCache.get(token);
        if (e == null) return null;
        if (System.currentTimeMillis() - e.cachedAt() > CACHE_TTL_MS) {
            sessionCache.remove(token);
            return null;
        }
        return e.user();
    }

    public boolean register(UserRequestDTO req) {

        if (userDAO.exists(req.getUsername())) {
            return false;
        }

        // Cost 10 ≈ 100 ms — still secure, 4× faster than cost 12 (≈ 400 ms)
        String hash = BCrypt.withDefaults().hashToString(10, req.getPassword().toCharArray());

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
        cacheToken(token, user);  // warm the cache immediately — first request is free

        return token;
    }

    public User authenticate(String token) {
        if (token == null || token.isEmpty()) return null;

        // Fast path: return cached user without touching the DB
        User cached = getCached(token);
        if (cached != null) return cached;

        // Slow path: DB lookup (only on first call or after cache expiry)
        User user = userDAO.findUserByToken(token);
        if (user != null) cacheToken(token, user);
        return user;
    }

    public void logout(String token) {
        if (token != null) {
            sessionCache.remove(token);  // evict immediately on logout
            userDAO.deleteSession(token);
        }
    }

    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    public void setUserStatus(int userId, String status) {
        userDAO.updateStatus(userId, status);
    }
    public List<User> getAllUsers() {
        return userDAO.findAll();
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
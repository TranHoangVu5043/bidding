package Server.dao.users;

import Server.model.users.User;
import Server.model.users.UserFactory;
import Server.model.users.records.UserRow;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private final DataSource dataSource;

    public UserDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // ===== SHARED SQL FRAGMENT =====
    // Every user query LEFT JOINs user_settings so mapRow() always gets the prefs columns.
    private static final String SELECT_USER_WITH_SETTINGS = """
        SELECT u.*,
               COALESCE(s.notif_auction, TRUE)  AS notif_auction,
               COALESCE(s.notif_email,   FALSE) AS notif_email
        FROM   users u
        LEFT   JOIN user_settings s ON u.id = s.user_id
    """;

    // ===== USER METHODS =====

    public User findByUsername(String username) {
        String sql = SELECT_USER_WITH_SETTINGS + "WHERE u.username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            log("findByUsername failed", e);
        }
        return null;
    }

    public User findById(int id) {
        String sql = SELECT_USER_WITH_SETTINGS + "WHERE u.id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            log("findById failed", e);
        }
        return null;
    }

    public User findByUsernameAndPassword(String username, String password) {
        String sql = SELECT_USER_WITH_SETTINGS + "WHERE u.username = ? AND u.password = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            log("findByUsernameAndPassword failed", e);
        }
        return null;
    }

    /** Creates a user and returns the generated DB id (-1 on failure). */
    public int createUser(User user) {
        String sql = """
            INSERT INTO users(username, password, email, role, balance)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole());
            stmt.setDouble(5, user.getBalance());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }

        } catch (SQLException e) {
            log("createUser failed", e);
        }
        return -1;
    }

    /** Inserts a default row into user_settings for a newly registered user. */
    public void createDefaultSettings(int userId) {
        String sql = """
            INSERT INTO user_settings (user_id)
            VALUES (?)
            ON CONFLICT (user_id) DO NOTHING
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log("createDefaultSettings failed", e);
        }
    }


    public void deleteUser(int id) {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("deleteUser failed", e);
        }
    }

    public void addBalance(int userId, double amount) {
        String sql = "UPDATE users SET balance = balance + ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[UserDAO] addBalance: " + e.getMessage());
        }
    }

    public void updateBalance(int userId, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("[ERROR] updateBalance failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /** Transaction-aware balance update — caller owns the connection. */
    public void updateBalance(Connection conn, int userId, double newBalance) throws SQLException {
        String sql = "UPDATE users SET balance = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    /** Read user inside an existing transaction. */
    public User findById(Connection conn, int id) throws SQLException {
        String sql = SELECT_USER_WITH_SETTINGS + "WHERE u.id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public void updateStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log("updateStatus failed", e);
        }
    }

    public void updatePassword(int userId, String newHash) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newHash);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("updatePassword failed", e);
            throw new RuntimeException("Không thể cập nhật mật khẩu.", e);
        }
    }
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id ASC";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(mapRow(rs));
            }

        } catch (SQLException e) {
            log("findAll failed", e);
        }

        return users;
    }

    public boolean exists(String username) {
        String sql = "SELECT 1 FROM users WHERE username = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            log("exists check failed", e);
        }

        return false;
    }

    // ===== SESSION METHODS =====


    public void createSession(int userId, String token, LocalDateTime expiresAt) {
        String sql = "INSERT INTO sessions(user_id, token, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, token);
            stmt.setTimestamp(3, Timestamp.valueOf(expiresAt));
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("createSession failed", e);
        }
    }


    public User findUserByToken(String token) {
        String sql = """
            SELECT u.*,
                   COALESCE(s.notif_auction, TRUE)  AS notif_auction,
                   COALESCE(s.notif_email,   FALSE) AS notif_email
            FROM   users u
            JOIN   sessions sess ON u.id = sess.user_id
            LEFT   JOIN user_settings s ON u.id = s.user_id
            WHERE  sess.token = ? AND sess.expires_at > NOW()
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            log("findUserByToken failed", e);
        }
        return null;
    }


    public void deleteSession(String token) {
        String sql = "DELETE FROM sessions WHERE token = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, token);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("deleteSession failed", e);
        }
    }


    // ===== MAPPER =====

    public void updateNotifPrefs(int userId, boolean notifAuction, boolean notifEmail) {
        String sql = """
            INSERT INTO user_settings (user_id, notif_auction, notif_email)
            VALUES (?, ?, ?)
            ON CONFLICT (user_id) DO UPDATE
                SET notif_auction = EXCLUDED.notif_auction,
                    notif_email   = EXCLUDED.notif_email
        """;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setBoolean(2, notifAuction);
            stmt.setBoolean(3, notifEmail);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log("updateNotifPrefs failed", e);
        }
    }

    public void deleteAllSessions(int userId) {
        String sql = "DELETE FROM sessions WHERE user_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            log("deleteAllSessions failed", e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        UserRow ur = new UserRow(
                rs.getInt("id"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getDouble("balance"),
                rs.getString("store_name"),
                rs.getBoolean("notif_auction"),
                rs.getBoolean("notif_email")
        );

        return UserFactory.createUser(ur);
    }

    private void log(String message, Exception e) {
        System.err.println("[ERROR] " + message);
        System.err.println("Cause: " + e.getMessage());
    }
}
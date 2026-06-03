package Server.dao;

import Server.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    private static final Logger log = LoggerFactory.getLogger(NotificationDAO.class);

    private final DataSource dataSource;

    public NotificationDAO(DataSource ds) {
        this.dataSource = ds;
    }

    public void create(int userId, String message) {
        String sql = "INSERT INTO notifications(user_id, message) VALUES (?, ?)";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setString(2, message);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("create notification failed", e);
        }
    }

    public List<Notification> findByUserId(int userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC LIMIT 50";
        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) list.add(map(rs));

        } catch (SQLException e) {
            log.error("findByUserId failed", e);
        }
        return list;
    }

    public void markRead(int notificationId, int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE id = ? AND user_id = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, notificationId);
            ps.setInt(2, userId);

            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("markRead failed", e);
        }
    }

    public void markAllRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("markAllRead failed", e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        return new Notification(
            rs.getInt("id"),
            rs.getInt("user_id"),
            rs.getString("message"),
            rs.getBoolean("is_read"),
            rs.getTimestamp("created_at").toLocalDateTime().toString()
        );
    }
}

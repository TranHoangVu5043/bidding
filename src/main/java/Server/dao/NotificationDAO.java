package Server.dao;

import Server.model.Notification;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
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
            System.err.println("[NotificationDAO] create: " + e.getMessage());
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
            System.err.println("[NotificationDAO] findByUserId: " + e.getMessage());
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
            System.err.println("[NotificationDAO] markRead: " + e.getMessage());
        }
    }

    public void markAllRead(int userId) {
        String sql = "UPDATE notifications SET is_read = TRUE WHERE user_id = ?";

        try (Connection c = dataSource.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, userId);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[NotificationDAO] markAllRead: " + e.getMessage());
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

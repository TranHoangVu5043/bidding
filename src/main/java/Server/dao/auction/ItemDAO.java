package Server.dao.auction;


import Server.model.auction.items.Item;

import javax.sql.DataSource;
import java.sql.*;

public class ItemDAO {

    private final DataSource dataSource;

    public ItemDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Item findById(int id) {
        String sql = "SELECT * FROM items WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            log("findById item failed", e);
        }

        return null;
    }

    public void create(Item item) {
        String sql = """
            INSERT INTO items(name, description, owner_id, category, condition)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getOwnerId());
            stmt.setString(4, item.getCategory());
            stmt.setString(5, item.getCondition());

            stmt.executeUpdate();

        } catch (SQLException e) {
            log("create item failed", e);
        }
    }

    private Item mapRow(ResultSet rs) throws SQLException {
        return new Item(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("owner_id"),
                rs.getString("category"),
                rs.getString("condition")
        );
    }

    private void log(String msg, Exception e) {
        System.err.println("[ERROR] " + msg + ": " + e.getMessage());
    }
}
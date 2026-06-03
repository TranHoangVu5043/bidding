package Server.dao.auction;

import Server.model.auction.ItemFactory;
import Server.model.auction.items.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {

    private static final Logger log = LoggerFactory.getLogger(ItemDAO.class);

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
            log.error("findById item failed", e);
        }

        return null;
    }

    public List<Item> findAll() {
        String sql = "SELECT * FROM items ORDER BY id ASC";
        List<Item> items = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) items.add(mapRow(rs));
        } catch (SQLException e) {
            log.error("findAll items failed", e);
        }
        return items;
    }

    public List<Item> findByOwnerId(int ownerId) {
        String sql = "SELECT * FROM items WHERE owner_id = ?";
        List<Item> items = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) items.add(mapRow(rs));

        } catch (SQLException e) {
            log.error("findByOwnerId failed", e);
        }

        return items;
    }

    public Item create(Item item) {
        String sql = """
        INSERT INTO items(name, description, owner_id, category, condition, price, stock)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setInt(3, item.getOwnerId());
            stmt.setString(4, item.getCategory());
            stmt.setString(5, item.getCondition());
            stmt.setDouble(6, item.getPrice());
            stmt.setInt(7, item.getStock());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    item.setId(keys.getInt(1));
                }
            }
            return item;

        } catch (SQLException e) {
            log.error("create item failed", e);
            return null;
        }
    }

    public void update(Item item){
        String sql = """
                UPDATE items
                SET name = ?, description = ?, category = ?, condition = ?, price = ?, stock = ?
                WHERE id = ?
                """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, item.getName());
            stmt.setString(2, item.getDescription());
            stmt.setString(3, item.getCategory());
            stmt.setString(4, item.getCondition());
            stmt.setDouble(5, item.getPrice());
            stmt.setInt(6, item.getStock());
            stmt.setInt(7, item.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            log.error("update item failed", e);
        }
    }
    public void delete(int id){
        String sql = "DELETE FROM items WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }catch (SQLException e){
            log.error("delete item failed", e);
        }

    }

    private Item mapRow(ResultSet rs) throws SQLException {
        // 1. Lấy giá trị category từ database
        String category = rs.getString("category");

        // 2. Xử lý an toàn nếu dữ liệu bị NULL
        if (category == null || category.trim().isEmpty()) {
            category = "UNCLASSIFIED";
        }

        // 3. Truyền biến đã xử lý an toàn vào Factory
        return ItemFactory.createItem(
                category,
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getInt("owner_id"),
                rs.getString("condition"),
                rs
        );
    }

}
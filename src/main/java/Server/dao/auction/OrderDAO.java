package Server.dao.auction;

import Server.model.auction.items.Order;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    private final DataSource dataSource;

    public OrderDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Order> findBySellerId(int sellerId) {
        String sql = "SELECT * FROM orders WHERE seller_id = ? ORDER BY created_at DESC";
        List<Order> orders = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) orders.add(mapRow(rs));

        } catch (SQLException e) {
            log("findBySellerId failed", e);
        }

        return orders;
    }

    public void create(Order order) {
        String sql = """
            INSERT INTO orders(seller_id, buyer_id, product_name, total_amount, status)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, order.getSellerId());
            stmt.setInt(2, order.getBuyerId());
            stmt.setString(3, order.getProductName());
            stmt.setDouble(4, order.getTotalAmount());
            stmt.setString(5, order.getStatus());
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("create order failed", e);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getLong("id"));
        order.setSellerId(rs.getInt("seller_id"));
        order.setBuyerId(rs.getInt("buyer_id"));
        order.setProductName(rs.getString("product_name"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return order;
    }

    private void log(String msg, Exception e) {
        System.err.println("[ERROR] " + msg + ": " + e.getMessage());
    }
}

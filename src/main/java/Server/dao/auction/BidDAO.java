package Server.dao.auction;

import javax.sql.DataSource;
import java.sql.*;

public class BidDAO {

    private final DataSource dataSource;

    public BidDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void create(Connection conn, int userId, int auctionId, double amount) {
        String sql = """
            INSERT INTO bids(user_id, auction_id, amount, created_at)
            VALUES (?, ?, ?, NOW())
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, auctionId);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Integer findHighestBidder(Connection conn, int auctionId) {
        String sql = """
            SELECT user_id FROM bids
            WHERE auction_id = ?
            ORDER BY amount DESC
            LIMIT 1
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return rs.getInt("user_id");

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public
}
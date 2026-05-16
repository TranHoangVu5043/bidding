package Server.dao.auction;

import Server.model.auction.Bid;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Bid> getBidsByAuction(int auctionId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE auction_id = ? ORDER BY amount DESC";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, auctionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) bids.add(mapRow(rs));

        } catch (SQLException e) {
            System.err.println("[ERROR] getBidsByAuction failed: " + e.getMessage());
        }

        return bids;
    }

    public Integer findHighestBidder(int auctionId) {
        String sql = "SELECT user_id FROM bids WHERE auction_id = ? ORDER BY amount DESC LIMIT 1";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, auctionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("user_id");

        } catch (SQLException e) {
            System.err.println("[ERROR] findHighestBidder failed: " + e.getMessage());
        }

        return null;
    }


    private Bid mapRow(ResultSet rs) throws SQLException {
        return new Bid(
                rs.getInt("id"),
                rs.getInt("auction_id"),
                rs.getInt("user_id"),
                rs.getDouble("amount"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
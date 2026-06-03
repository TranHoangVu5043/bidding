package Server.dao.auction;

import Server.model.auction.Bid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {

    private static final Logger log = LoggerFactory.getLogger(BidDAO.class);

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
            log.error("getBidsByAuction failed", e);
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
            log.error("findHighestBidder failed", e);
        }

        return null;
    }
    public void updateEndtime(Connection conn, int auctionId, LocalDateTime newEndtime){
        String sql = "UPDATE auctions SET end_Time = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp new_Endtime = java.sql.Timestamp.valueOf(newEndtime);
            stmt.setTimestamp(1, new_Endtime);
            stmt.setInt(2, auctionId);
            stmt.executeUpdate();
        }catch (SQLException e){
            throw new RuntimeException("Lỗi gia hạn phiên", e);
        }
    }


    public double getMaxBidByUser(int userId, int auctionId) {
        String sql = "SELECT COALESCE(MAX(amount), 0) FROM bids WHERE user_id = ? AND auction_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            log.error("getMaxBidByUser failed", e);
        }
        return 0;
    }

    /** Deletes all bids for an auction (call before deleting the auction row). */
    public void deleteByAuctionId(int auctionId) {
        String sql = "DELETE FROM bids WHERE auction_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, auctionId);
            ps.executeUpdate();
        } catch (SQLException e) {
            log.error("deleteByAuctionId failed", e);
        }
    }

    /** Transaction-aware version — caller owns the connection. */
    public double getMaxBidByUser(Connection conn, int userId, int auctionId) throws SQLException {
        String sql = "SELECT COALESCE(MAX(amount), 0) FROM bids WHERE user_id = ? AND auction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        }
        return 0;
    }

    public int getBidCountByUser(int userId, int auctionId) {
        String sql = "SELECT COUNT(*) FROM bids WHERE user_id = ? AND auction_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, auctionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            log.error("getBidCountByUser failed", e);
        }
        return 0;
    }

    private Bid mapRow(ResultSet rs) throws SQLException {
        return new Bid(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("auction_id"),
                rs.getDouble("amount"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
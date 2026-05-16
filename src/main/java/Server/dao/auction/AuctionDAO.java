package Server.dao.auction;


import Server.model.auction.Auction;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuctionDAO {

    private final DataSource dataSource;

    public AuctionDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Auction findById(int id) {
        String sql = "SELECT * FROM auctions WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            log("find auction failed", e);
        }

        return null;
    }

    /** Used inside a transaction — caller owns the connection and lock. */
    public Auction findByIdForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM auctions WHERE id = ? FOR UPDATE";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        }
        return null;
    }

    public List<Auction> findAll() {
        String sql = "SELECT * FROM auctions ORDER BY end_time ASC";
        List<Auction> auctions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) auctions.add(mapRow(rs));

        } catch (SQLException e) {
            log("findAll auctions failed", e);
        }

        return auctions;
    }

    public List<Auction> findByOwnerId(int ownerId) {
        String sql = "SELECT * FROM auctions WHERE owner_id = ? ORDER BY end_time ASC";
        List<Auction> auctions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ownerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) auctions.add(mapRow(rs));

        } catch (SQLException e) {
            log("findByOwnerId auctions failed", e);
        }

        return auctions;
    }

    public boolean create(Auction auction) {
        String sql = """
            INSERT INTO auctions(item_id, owner_id, starting_price, current_price, start_time, end_time, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, auction.getItemId());
            stmt.setInt(2, auction.getOwnerId());
            stmt.setDouble(3, auction.getStartingPrice());
            stmt.setDouble(4, auction.getCurrentPrice());
            stmt.setTimestamp(5, Timestamp.valueOf(auction.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(auction.getEndTime()));
            stmt.setString(7, auction.getStatus());

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            log("create auction failed", e);
            return false;
        }
    }

    public void updateCurrentPrice(int id, double price) {
        String sql = "UPDATE auctions SET current_price = ? WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, price);
            stmt.setInt(2, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("update auction price failed", e);
        }
    }

    /** Transaction-aware price update — caller owns the connection. */
    public void updateCurrentPrice(Connection conn, int id, double price) throws SQLException {
        String sql = "UPDATE auctions SET current_price = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, price);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void deleteAuction(int id) {
        String sql = "DELETE FROM auctions WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            log("delete auction failed", e);
        }
    }

    public boolean updateStatus(int auctionId, String status) {
        String sql = "UPDATE auctions SET status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, auctionId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            log("update auction status failed for ID: " + auctionId, e);
            return false;
        }
    }

    private Auction mapRow(ResultSet rs) throws SQLException {
        return new Auction(
                rs.getInt("id"),
                rs.getInt("item_id"),
                rs.getInt("owner_id"),
                rs.getDouble("starting_price"),
                rs.getDouble("current_price"),
                rs.getTimestamp("start_time").toLocalDateTime(),
                rs.getTimestamp("end_time").toLocalDateTime(),
                rs.getString("status")
        );
    }

    private void log(String msg, Exception e) {
        System.err.println("[ERROR] " + msg + ": " + e.getMessage());
    }
}

package Server.dao.auction;


import Server.model.auction.Auction;

import javax.sql.DataSource;
import java.sql.*;

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

    public boolean create(Auction auction) {
        String sql = """
            INSERT INTO auctions(item_id, owner_id, starting_price, current_price, end_time)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, auction.getItemId());
            stmt.setInt(2, auction.getOwnerId());
            stmt.setDouble(3, auction.getStartingPrice());
            stmt.setDouble(4, auction.getCurrentPrice());
            stmt.setTimestamp(5, Timestamp.valueOf(auction.getEndTime()));

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            log("create auction failed", e);
            return  false;
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

    public void deleteAuction(int id) {
        String sql = "DELETE FROM auctions WHERE id = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeQuery();

        } catch (SQLException e) {
            log("delete auction failed", e);
        }
    }
    public boolean updateStatus(int auctionId, String status){
        String sql = "UPDATE auction SET current status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, auctionId);

            stmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            log("Lỗi cập nhật trạng thái đấu giá ID: " + auctionId, e);
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
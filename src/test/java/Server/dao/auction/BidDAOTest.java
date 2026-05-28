package Server.dao.auction;

import Server.model.auction.Bid;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BidDAOTest {

    private JdbcDataSource dataSource;
    private Connection conn;
    private BidDAO bidDAO;

    @BeforeEach
    public void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        conn = dataSource.getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS auctions (" +
                    "id SERIAL PRIMARY KEY, " +
                    "end_time TIMESTAMP" +
                    ")");
            stmt.execute("CREATE TABLE IF NOT EXISTS bids (" +
                    "id SERIAL PRIMARY KEY, " +
                    "auction_id INT, " +
                    "user_id INT, " +
                    "amount DOUBLE, " +
                    "created_at TIMESTAMP" +
                    ")");
        }
        bidDAO = new BidDAO(dataSource);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS bids");
                stmt.execute("DROP TABLE IF EXISTS auctions");
            }
            conn.close();
        }
    }
    @Test
    public void testCreateBid_Success() throws Exception {
        bidDAO.create(conn, 10, 100, 550.0);
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM bids WHERE auction_id = 100")) {
            assertTrue(rs.next(), "Phải tìm thấy lượt ra giá vừa tạo");
            assertEquals(10, rs.getInt("user_id"));
            assertEquals(550.0, rs.getDouble("amount"));
        }
    }

    @Test
    public void testGetBidsByAuction_Success() throws Exception {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO bids (user_id ,auction_id,  amount, created_at) VALUES (11, 100,  500.0, '" + now + "')");
            stmt.execute("INSERT INTO bids (user_id ,auction_id, amount, created_at) VALUES (12, 100, 600.0, '" + now + "')");
        }
        List<Bid> bids = bidDAO.getBidsByAuction(100);

        assertNotNull(bids);
        assertEquals(2, bids.size());
        assertEquals(12, bids.get(0).getUserId(), "Thằng trả giá cao nhất phải xếp đầu tiên");
    }

    @Test
    public void testFindHighestBidder_Success() throws Exception {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO bids (user_id ,auction_id, amount, created_at) VALUES (200, 16, 1000.0, '" + now + "')");
            stmt.execute("INSERT INTO bids (user_id ,auction_id, amount, created_at) VALUES (201, 16, 1500.0, '" + now + "')");
        }
        Integer highestBidderId = bidDAO.findHighestBidder(16);

        assertNotNull(highestBidderId);
        assertEquals(201, highestBidderId);
    }

    @Test
    public void testUpdateEndtime_Success() throws Exception {
        int auctionId = 50;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO auctions (id, end_time) VALUES (" + auctionId + ", NOW())");
        }

        LocalDateTime newEndtime = LocalDateTime.now().plusHours(2);
        bidDAO.updateEndtime(conn, auctionId, newEndtime);

        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT end_time FROM auctions WHERE id = " + auctionId)) {

            assertTrue(rs.next());
            assertNotNull(rs.getTimestamp("end_time"));
        }
    }
}
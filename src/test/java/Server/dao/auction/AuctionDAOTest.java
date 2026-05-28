package Server.dao.auction;

import Server.model.auction.Auction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionDAOTest {

    private JdbcDataSource dataSource;
    private Connection conn;
    private AuctionDAO auctionDAO;

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
                    "item_id INT, " +
                    "owner_id INT, " +
                    "START_PRICE DOUBLE, " +
                    "starting_price DOUBLE, " +
                    "current_price DOUBLE, " +
                    "start_time TIMESTAMP, " +
                    "end_time TIMESTAMP, " +
                    "status VARCHAR(50)" +
                    ")");
        }

        auctionDAO = new AuctionDAO(dataSource);
    }

    @Test
    public void testUpdateCurrentPrice_Success() throws Exception {
        int auctionId = 100;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp tomorrow = Timestamp.valueOf(LocalDateTime.now().plusDays(1));

        try (Statement stmt = conn.createStatement()) {
            String sql = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status) " +
                    "VALUES (" + auctionId + ", 1, 1, 100.0, 100.0, 100.0, '" + now + "', '" + tomorrow + "', 'ACTIVE')";
            stmt.execute(sql);
        }

        auctionDAO.updateCurrentPrice(conn, auctionId, 150.0);

        Auction result = auctionDAO.findByIdForUpdate(conn, auctionId);

        assertNotNull(result, "Phòng đấu giá phải tồn tại trong DB RAM");
        assertEquals(150.0, result.getCurrentPrice(), "Giá hiện tại phải được cập nhật chính xác thành 150.0");
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (conn != null) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS auctions");
            }
            conn.close();
        }
    }
    @Test
    public void testCreateAuction() throws Exception{
        LocalDateTime now = LocalDateTime.now();
        Auction auc = new Auction(1,1,1,100,100, now,now.plusDays(9), "PENDING" );
        Auction saveact = auctionDAO.create(auc);
        assertNotNull(saveact, "Tạo thành công");
        Auction checkAuc = auctionDAO.findById(1);
        assertNotNull(checkAuc, "Phải tồn tại");
        assertEquals("PENDING", checkAuc.getStatus());
        assertEquals(100, checkAuc.getCurrentPrice());
    }
    @Test
    public void testFindAllAuction() throws Exception{
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp tomorrow = Timestamp.valueOf(LocalDateTime.now().plusDays(9));
        try(Statement stmt = conn.createStatement()){
            String sql1 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (1, 1, 1, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'ACTIVE')";
            String sql2 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (2, 2, 2, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'ACTIVE')";
            stmt.execute(sql1);
            stmt.execute(sql2);
        }
        List<Auction> list = auctionDAO.findAll();
        assertNotNull(list);
        assertEquals(2, list.size());
    }
    @Test
    public  void testFindbyOwnerId() throws Exception{
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp tomorrow = Timestamp.valueOf(LocalDateTime.now().plusDays(9));
        try(Statement stmt = conn.createStatement()){
            String sql1 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (1, 1, 1, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'ACTIVE')";
            String sql2 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (2, 2, 2, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'ACTIVE')";
            stmt.execute(sql1);
            stmt.execute(sql2);
        }
        List<Auction> list = auctionDAO.findByOwnerId(1);
        assertNotNull(list);
        assertEquals(1, list.size());
        List<Auction> list1 = auctionDAO.findByOwnerId(3);
        assertEquals(0, list1.size());
    }
    @Test
    public void testUpdateCurrentPrice() throws SQLException {
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp tomorrow = Timestamp.valueOf(LocalDateTime.now().plusDays(9));
        try(Statement stmt = conn.createStatement()) {
            String sql1 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)" +
                    "VALUES (1, 1, 1, 100, 100, 100, '" + now + "','" + tomorrow + "', 'ACTIVE')";
            stmt.execute(sql1);
        }
        auctionDAO.updateCurrentPrice(conn, 1, 120.0);
        Auction auction = auctionDAO.findById(1);
        assertNotNull(auction);
        assertEquals(120, auction.getCurrentPrice());
    }
    @Test
    public  void testGetActiveAuction() throws Exception{
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp tomorrow = Timestamp.valueOf(LocalDateTime.now().plusDays(9));
        try(Statement stmt = conn.createStatement()){
            String sql1 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (1, 1, 1, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'ACTIVE')";
            String sql2 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (2, 2, 2, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'CANCELED')";
            stmt.execute(sql1);
            stmt.execute(sql2);
        }
        List<Auction> list = auctionDAO.getActiveAuctions();
        assertNotNull(list);
        assertEquals(1, list.size());
    }
    @Test
    public  void testDeleteAuction() throws Exception{
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Timestamp tomorrow = Timestamp.valueOf(LocalDateTime.now().plusDays(9));
        try(Statement stmt = conn.createStatement()){
            String sql1 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (1, 1, 1, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'ACTIVE')";
            String sql2 = "INSERT INTO auctions (id, item_id, owner_id, START_PRICE, starting_price, current_price, start_time, end_time, status)"+
                    "VALUES (2, 2, 2, 100, 100, 100, '" +now+ "','"+tomorrow+"', 'CANCELED')";
            stmt.execute(sql1);
            stmt.execute(sql2);
        }
        auctionDAO.deleteAuction(1);
        Auction auction = auctionDAO.findById(1);
        assertNull(auction);
    }
}
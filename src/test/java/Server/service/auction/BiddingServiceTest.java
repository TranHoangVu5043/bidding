package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.model.auction.Auction;
import Server.model.users.Bidder;
import Server.model.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BiddingServiceTest {

    private BiddingService biddingService;

    private FakeUserDAO fakeUserDAO;
    private FakeAuctionDAO fakeAuctionDAO;
    private FakeBidDAO fakeBidDAO;

    @BeforeEach
    public void setUp() {
        SimpleFakeDataSource safeDataSource = new SimpleFakeDataSource();

        // GIẢI QUYẾT LỖI DAO: Truyền safeDataSource vào constructor của DAO
        fakeUserDAO = new FakeUserDAO(safeDataSource);
        fakeAuctionDAO = new FakeAuctionDAO(safeDataSource);
        fakeBidDAO = new FakeBidDAO(safeDataSource);

        biddingService = new BiddingService(safeDataSource, fakeUserDAO, fakeAuctionDAO, fakeBidDAO);
    }

    @Test
    public void testPlaceBid_Success() {
        Auction sampleAuction = new Auction(0, 1, 1, 100.0, 100, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "ACTIVE") {
        };
        fakeAuctionDAO.setSampleAuction(sampleAuction);

        User sampleUser = new Bidder(0, "H", "1434D", "ASD@gmail.com",  1000) {
        };
        fakeUserDAO.setSampleUser(sampleUser);

        // CHẠY TEST
        assertDoesNotThrow(() -> biddingService.placeBid(0, 1, 200.0));

        assertEquals(800.0, fakeUserDAO.getUpdatedBalance(), "Ví user phải bị trừ đi 200$ còn 800$");
        assertEquals(200.0, fakeAuctionDAO.getUpdatedPrice(), "Giá phiên đấu giá phải tăng lên 200$");
        assertTrue(fakeBidDAO.isBidCreated(), "Lịch sử đặt thầu mới phải được tạo!");
    }

    @Test
    public void testPlaceBid_InsufficientBalance() {
        Auction sampleAuction = new Auction(0, 1, 1, 100.0, 1500, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2), "ACTIVE") {};
        fakeAuctionDAO.setSampleAuction(sampleAuction);

        User sampleUser = new Bidder(1, "BA", "12efv", "qqd@gmail.com", 100);
        fakeUserDAO.setSampleUser(sampleUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biddingService.placeBid(0, 1, 1600.0);
        });

        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }
}
class SimpleFakeDataSource implements javax.sql.DataSource {
    @Override
    public java.sql.Connection getConnection() throws java.sql.SQLException {
        return (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(
                java.sql.Connection.class.getClassLoader(),
                new Class<?>[]{java.sql.Connection.class},
                (proxy, method, args) -> null
        );
    }
    @Override public java.sql.Connection getConnection(String username, String password) throws java.sql.SQLException { return getConnection(); }
    @Override public java.io.PrintWriter getLogWriter() { return null; }
    @Override public void setLogWriter(java.io.PrintWriter out) {}
    @Override public void setLoginTimeout(int seconds) {}
    @Override public int getLoginTimeout() { return 0; }
    @Override public java.util.logging.Logger getParentLogger() { return null; }
    @Override public <T> T unwrap(Class<T> iface) { return null; }
    @Override public boolean isWrapperFor(Class<?> iface) { return false; }
}


class FakeUserDAO extends UserDAO {
    private User sampleUser;
    private double updatedBalance;

    public FakeUserDAO(javax.sql.DataSource ds) {
        super(ds);
    }

    public void setSampleUser(User u) { this.sampleUser = u; }
    public double getUpdatedBalance() { return updatedBalance; }

    @Override public User findById(java.sql.Connection conn, int id) { return this.sampleUser; }
    @Override public void updateBalance(java.sql.Connection conn, int id, double balance) { this.updatedBalance = balance; }
}

class FakeAuctionDAO extends AuctionDAO {
    private Auction sampleAuction;
    private double updatedPrice;

    // Sửa lỗi: Gọi super() và truyền DataSource vào cho đúng constructor của Vũ
    public FakeAuctionDAO(DataSource ds) {
        super(ds);
    }

    public void setSampleAuction(Auction a) { this.sampleAuction = a; }
    public double getUpdatedPrice() { return updatedPrice; }

    @Override public Auction findByIdForUpdate(java.sql.Connection conn, int id) { return this.sampleAuction; }
    @Override public void updateCurrentPrice(java.sql.Connection conn, int id, double price) { this.updatedPrice = price; }
}

class FakeBidDAO extends BidDAO {
    private boolean bidCreated = false;
    public boolean isBidCreated() { return bidCreated; }

    public FakeBidDAO(javax.sql.DataSource ds) {
        super(ds);
    }

    @Override
    public void create(java.sql.Connection conn, int userId, int auctionId, double amount) {
        this.bidCreated = true;
    }

    @Override
    public void updateEndtime(java.sql.Connection conn, int auctionId, java.time.LocalDateTime newEndtime) {
    }
}
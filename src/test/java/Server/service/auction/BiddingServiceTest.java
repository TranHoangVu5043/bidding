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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.logging.Logger;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.junit.jupiter.api.Assertions.*;

public class BiddingServiceTest {

    private BiddingService biddingService;

    private FakeUserDAO fakeUserDAO;
    private FakeAuctionDAO fakeAuctionDAO;
    private FakeBidDAO fakeBidDAO;

    @BeforeEach
    public void setUp() {
        SimpleFakeDataSource safeDataSource = new SimpleFakeDataSource();

        fakeUserDAO = new FakeUserDAO(safeDataSource);
        fakeAuctionDAO = new FakeAuctionDAO(safeDataSource);
        fakeBidDAO = new FakeBidDAO(safeDataSource);

        biddingService = new BiddingService(safeDataSource, fakeUserDAO, fakeAuctionDAO, fakeBidDAO);
    }

    @Test
    public void testPlaceBid_Success() {
        Auction sampleAuction = new Auction(0, 1, 1, 100.0, 100, LocalDateTime.now(), LocalDateTime.now().plusDays(9), "ACTIVE") {
        };
        fakeAuctionDAO.setSampleAuction(sampleAuction);
        User sampleUser = new Bidder(0, "H", "1434D", "ASD@gmail.com",  1000) {
        };
        fakeUserDAO.setSampleUser(sampleUser);

        assertDoesNotThrow(() -> biddingService.placeBid(0, 0, 200.0));

        assertEquals(800.0, fakeUserDAO.getUpdatedBalance(), "Ví user phải bị trừ đi 200$ còn 800$");
        assertEquals(200.0, fakeAuctionDAO.getUpdatedPrice(), "Giá phiên đấu giá phải tăng lên 200$");
        assertTrue(fakeBidDAO.isBidCreated(), "Lịch sử đặt thầu mới phải được tạo!");
    }

    @Test
    public void testPlaceBid_InsufficientBalance() {
        Auction sampleAuction = new Auction(0, 1, 1, 100.0, 1500, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(9), "ACTIVE") {};
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
    public Connection getConnection() throws SQLException {
        return (Connection) newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> null
        );
    }
    @Override public Connection getConnection(String username, String password) throws java.sql.SQLException { return getConnection(); }
    @Override public PrintWriter getLogWriter() { return null; }
    @Override public void setLogWriter(java.io.PrintWriter out) {}
    @Override public void setLoginTimeout(int seconds) {}
    @Override public int getLoginTimeout() { return 0; }
    @Override public Logger getParentLogger() { return null; }
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

    @Override public User findById(Connection conn, int id) { return this.sampleUser; }
    @Override public void updateBalance(int id, double balance){this.updatedBalance = balance;}
    @Override public void updateBalance(Connection conn, int id, double balance) { this.updatedBalance = balance; }
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

    @Override public Auction findByIdForUpdate(Connection conn, int id) { return this.sampleAuction; }
    @Override public void updateCurrentPrice(Connection conn, int id, double price) { this.updatedPrice = price; }
}

class FakeBidDAO extends BidDAO {
    private boolean bidCreated = false;
    private Integer highBidder;
    public boolean isBidCreated() { return bidCreated; }

    public FakeBidDAO(DataSource ds) {
        super(ds);
    }

    @Override
    public Integer findHighestBidder (int auctionId){
       return null;
    }

    @Override
    public void create(Connection conn, int userId, int auctionId, double amount) {
        this.bidCreated = true;
    }

    @Override
    public void updateEndtime(Connection conn, int auctionId, LocalDateTime newEndtime) {
    }
}
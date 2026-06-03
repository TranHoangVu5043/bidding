package Server.service.auction;

import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.model.users.Bidder;
import Server.model.users.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AutoBidConfigServiceTest {

    private AutoBidConfigService autoBidConfigService;
    private AFakeBiddingService fakeBiddingService;

    @BeforeEach
    public void setUp() {
        fakeBiddingService = new AFakeBiddingService();
        autoBidConfigService = new AutoBidConfigService(fakeBiddingService);
    }

    @Test
    public void testRegisterAutoBid_Succsess() {
        autoBidConfigService.registerAutoBid(1, 1, 1000, 10);

        // 2. Giả lập giá phòng hiện tại là 100.0
        fakeBiddingService.setMockCurrentPrice(100.0);

        // 3. Kích hoạt hệ thống điều phối Bot chạy ngầm
        autoBidConfigService.triggerAutoBidding(1);

        // 4. Kiểm tra kết quả bẫy được: Giá phải bằng Giá hiện tại (100) + Bước giá (10) = 110
        assertTrue(fakeBiddingService.isPlaceBidInternalCalled, "Bot phải được kích hoạt chạy");
        assertEquals(1, fakeBiddingService.capturedWinnerBotId);
        assertEquals(100.0, fakeBiddingService.capturedFinalPrice, "Chưa ai vào đấu, bot đứng đầu");
    }

    @Test
    public void testTriggerAutoBidding_TwoBots_EqualMaxBid() {
        int auctionId = 1;
        fakeBiddingService.setMockCurrentPrice(100.0);
        autoBidConfigService.registerAutoBid(auctionId, 1, 500.0, 10.0);
        autoBidConfigService.registerAutoBid(auctionId, 2, 500.0, 20.0);

        autoBidConfigService.triggerAutoBidding(auctionId);
        assertTrue(fakeBiddingService.isPlaceBidInternalCalled);
        assertEquals(1, fakeBiddingService.capturedWinnerBotId, "Bot 1 xếp đầu hàng đợi thắng");
        assertEquals(500.0, fakeBiddingService.capturedFinalPrice, "Giá cuối cùng phải đẩy thẳng lên Max là 500.0");
    }

    @Test
    public void testTriggerAutoBidding_TwoBots_DifferentMaxBid() {
        int auctionId = 1;
        fakeBiddingService.setMockCurrentPrice(100.0);

        autoBidConfigService.registerAutoBid(auctionId, 1, 1000.0, 10.0);
        autoBidConfigService.registerAutoBid(auctionId, 2, 400.0, 20.0);

        autoBidConfigService.triggerAutoBidding(auctionId);

        assertTrue(fakeBiddingService.isPlaceBidInternalCalled);
        assertEquals(1, fakeBiddingService.capturedWinnerBotId, "Bot 1 nhiều tiền hơn phải thắng");

        assertEquals(410, fakeBiddingService.capturedFinalPrice);
    }
}
class AFakeBiddingService extends BiddingService {
    private double mockCurrentPrice = 0.0;

    boolean isPlaceBidInternalCalled = false;
    int capturedWinnerBotId = -1;
    double capturedFinalPrice = -1.0;

    private final BidDAO mockBidDAO = mock(BidDAO.class);
    private final UserDAO mockUserDAO = mock(UserDAO.class);
    public AFakeBiddingService() {
        super(new PGSimpleDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return (Connection) Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class<?>[] { Connection.class },
                        (proxy, method, args) -> null
                );
            }
        }, null, null, null);

        // 2. Cấu hình mặc định cho bản mock: Khi gọi tìm người đặt giá cao nhất, trả về null (chưa có ai đặt)
        when(mockBidDAO.findHighestBidder(anyInt())).thenReturn(null);
        try {
            User fakeUser = new Bidder(1, "a", "123", "12", 100000);
            // Cứ gọi tìm User với bất kỳ ID nào (anyInt) thì Mockito đều trả về User 100 triệu này
            org.mockito.Mockito.when(mockUserDAO.findById(any(), anyInt())).thenReturn(fakeUser);
        }catch (Exception e){
            e.getMessage();
        }
    }

    @Override
    public BidDAO getBidDAO() {
        return this.mockBidDAO;
    }

    public void setMockCurrentPrice(double price) {
        this.mockCurrentPrice = price;
    }

    @Override
    public double getCurrentPriceforUpdate(Connection conn, int auctionId) throws Exception {
        return this.mockCurrentPrice;
    }

    @Override
    public void placeBidInternal(Connection conn, int userId, int auctionId, double price) throws Exception {
        this.isPlaceBidInternalCalled = true;
        this.capturedWinnerBotId = userId;
        this.capturedFinalPrice = price;

        // Cập nhật luôn giá hiện tại để vòng lặp của Bot nhận biết được giá mới đang tăng lên
        this.mockCurrentPrice = price;
    }
    @Override
    public UserDAO getUserDAO(){return this.mockUserDAO;}
}
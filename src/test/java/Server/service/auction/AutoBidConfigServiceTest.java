package Server.service.auction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

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
        // 1. Đăng ký Bot: phòng số 1, ví max 1000, mỗi lần tăng 10
        autoBidConfigService.registerAutoBid(1, 1, 1000, 10);

        // 2. Giả lập giá phòng hiện tại là 100.0
        fakeBiddingService.setMockCurrentPrice(100.0);

        // 3. Kích hoạt hệ thống điều phối Bot chạy ngầm
        autoBidConfigService.triggerAutoBidding(1);

        // 4. Kiểm tra kết quả bẫy được: Giá phải bằng Giá hiện tại (100) + Bước giá (10) = 110
        assertTrue(fakeBiddingService.isPlaceBidInternalCalled, "Bot phải được kích hoạt chạy");
        assertEquals(1, fakeBiddingService.capturedWinnerBotId);
        assertEquals(110.0, fakeBiddingService.capturedFinalPrice, "Giá đấu mới của Bot phải là 110.0");
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

    // 1. Tạo một bản mock BidDAO "xịn" bằng Mockito
    private final Server.dao.auction.BidDAO mockBidDAO = org.mockito.Mockito.mock(Server.dao.auction.BidDAO.class);

    public AFakeBiddingService() {
        super(new PGSimpleDataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return (Connection) java.lang.reflect.Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class<?>[] { Connection.class },
                        (proxy, method, args) -> null
                );
            }
        }, null, null, null);

        // 2. Cấu hình mặc định cho bản mock: Khi gọi tìm người đặt giá cao nhất, trả về null (chưa có ai đặt)
        org.mockito.Mockito.when(mockBidDAO.findHighestBidder(org.mockito.Mockito.anyInt())).thenReturn(null);
    }

    // 3. IMPORTANT: Override lại hàm getBidDAO để cứu hệ thống khỏi lỗi NullPointerException
    @Override
    public Server.dao.auction.BidDAO getBidDAO() {
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
}
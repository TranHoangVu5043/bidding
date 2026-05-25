package Server;


import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.networking.DataSourceFactory;
import Server.service.auction.AutoBidConfigService;
import Server.service.auction.BiddingService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.desktop.SystemSleepEvent;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for simple App.
 */
public class ServerAppTest
{
    private BiddingService biddingService;
    @BeforeEach
    void setBiddingService(){
        var ds = DataSourceFactory.getDataSource();
        var u = new UserDAO(ds);
        var a = new AuctionDAO(ds);
        var b = new BidDAO(ds);
        this.biddingService = new BiddingService(ds, u, a, b );
        var autoBid = new AutoBidConfigService(biddingService);
        biddingService.setAutoBidConfigService(autoBid);
    }

    @Test
    public  void testlogicDatgia(){
        int userId = 1;
        int auctionId = 1;
        double amount = 3000; //nào test thì sang supabase check currentPrice xong đổi giá tí nhé
        assertDoesNotThrow(() -> {
            biddingService.placeBid(userId, auctionId, amount);
        });
        System.out.println(">>> [TEST SUCCESS PASSED]: Tài khoản john đã đặt giá "+ amount + " thành công!");
    }
    @Test
    public void testInsufficientBalance(){
        int userId = 1;
        int auctionId = 1;
        double amount = 22300;
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biddingService.placeBid(userId, auctionId, amount);
        });
        String message = exception.getMessage();
        String expectedMessage = "Insufficient balance";
        assertTrue(message.contains(expectedMessage));
        System.out.println(">>> [TEST PASSED]: Chặn thành công tài khoản không đủ tiền, lỗi: " + message );

    }
    @Test
    public void testBidamounttoLow(){
        int userId = 1;
        int auctionId = 5;
        double amount = 10;
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biddingService.placeBid(userId, auctionId, amount);
        });
        String message = exception.getMessage();
        String expectedMessage = "Bid must be higher than current price of";
        assertTrue(message.contains(expectedMessage), "Bắt lỗi: " + message);
        System.out.println(">>> [TEST PASSED]");

    }
    @Test
    public void testAuctionnotFound(){
        int userId = 1;
        int fakeauctionId = 6;
        double amount = 10001;
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biddingService.placeBid(userId, fakeauctionId, amount);
        });
        String message = exception.getMessage();
        String expectedMessage = "Auction not found";
        assertTrue(message.contains(expectedMessage), "Bắt lỗi: " + message);
        System.out.println(">>> [TEST PASSED]");

    }
    @Test
    public  void testAuctionnotActive(){
        int userId = 1;
        int fakeauctionId = 3; //trạng thái canceled
        double amount = 10001;
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            biddingService.placeBid(userId, fakeauctionId, amount);
        });
        String message = exception.getMessage();
        String expectedMessage = "Auction is not running";
        assertTrue(message.contains(expectedMessage), "Bắt lỗi: " + message);
        System.out.println(">>>[TEST PASSED]");

    }

    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
}

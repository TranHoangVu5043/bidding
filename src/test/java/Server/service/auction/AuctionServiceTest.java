
package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.dao.users.UserDAO;
import Server.model.auction.Auction;
import Server.model.auction.Bid;
import Server.model.auction.items.Art;
import Server.model.auction.items.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class AuctionServiceTest {

    private AuctionService auctionService;

    // Đội quân đóng thế cho các DAO
    private FakeAuctionDAO fakeAuctionDAO;
    private FakeBidDAO fakeBidDAO;
    private FakeItemDAO fakeItemDAO;
    private FakeUserDAO fakeUserDAO;

    @BeforeEach
    public void setUp() {
        SimpleFakeDataSource safeDataSource = new SimpleFakeDataSource();

        // Khởi tạo các DAO fake sạch lỗi constructor
        fakeAuctionDAO = new FakeAuctionDAO(safeDataSource);
        fakeBidDAO = new FakeBidDAO(safeDataSource);
        fakeItemDAO = new FakeItemDAO(safeDataSource);
        fakeUserDAO = new FakeUserDAO(safeDataSource);

        auctionService = new AuctionService(fakeAuctionDAO, fakeBidDAO, fakeItemDAO, fakeUserDAO, null);
    }
    @Test
    public void testAuctionNotFound(){
        fakeAuctionDAO.setSampleAuction(null);
        assertNull(auctionService.getAuction(1), "Không tồn tại, trả về null");
        assertFalse(auctionService.cancelAuction(1, 1),"Phiên không tồn tại, không thể hủy trả về false");
        assertNull(auctionService.finalizeAuction(1), "Phiên không tồn tại, không thể chôốt giá trả về null");
        assertDoesNotThrow(() ->
            auctionService.refreshAuctionStatus(1), "Không trả về gì"
        );
    }
    @Test
    public void testAuctionwasFound() {
        Auction auction = new Auction(1, 1, 1, 100, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(9), "UPCOMING");
        fakeAuctionDAO.setSampleAuction(auction);
        Auction auc = auctionService.getAuction(1);
        assertNotNull(auc, "Tồn tại không trả về null");
        assertEquals(auc.getId(), 1, "Xét xem trả đúng phiên không");
        auctionService.refreshAuctionStatus(1);
        assertEquals("ACTIVE", fakeAuctionDAO.getUpdatedStatus(), "Phải cập nhật trạng thái");
    }
    @Test
    public void testAuctionwasFound_Cancel(){
        Auction auction = new Auction(1, 1, 1, 100, 100, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(9), "UPCOMING");
        fakeAuctionDAO.setSampleAuction(auction);
        boolean cancel = auctionService.cancelAuction(1, 2);
        assertFalse(cancel, "Không có quyền hủy phiên" );
        boolean cancel2 = auctionService.cancelAuction(1, 1);
        assertTrue(cancel2, "Phiên đã bị hủy");

        Integer winnerId = auctionService.finalizeAuction(1);
        assertNull(winnerId, "Không ai đặt giá thì null");
    }
    @Test
    public  void testCreateAuction_ItemNotFound(){
        fakeItemDAO.setSampleItem(null);
        Auction auction = new Auction(1, 1, 1, 100, 100,LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(9), "ACTIVE" );
        Auction auc = auctionService.createAuction(auction, 1);
        assertNull(auc, "Vật phẩm không tồn tại");
    }
    @Test
    public void testCreateAuction(){
        Item item = new Art(1, "Hoa", null, 2, null, null, 10, 10);
        fakeItemDAO.setSampleItem(item);
        Auction auction = new Auction(1, 1, 2, 100, 100,LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(9), "UPCOMING" );
        Auction auc = auctionService.createAuction(auction, 2);
        assertNotNull(auc, "Tạo thành công");
        assertEquals("UPCOMING", auc.getStatus(),"HỢP LỆ" );
    }
    @Test
    public void testCreateAuction_NotTheOwner(){
        Item item = new Art(1, "Hoa", null, 1, null, null, 10, 10);
        fakeItemDAO.setSampleItem(item);
        Auction auction = new Auction(1, 1, 2, 100, 100,LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(9), "ACTIVE" );
        Auction auc = auctionService.createAuction(auction, 2);
        assertNull(auc, "Sai chủ sở hữu");
    }

    class SimpleFakeDataSource implements javax.sql.DataSource {
        @Override public Connection getConnection() throws SQLException { return null; }
        @Override public Connection getConnection(String u, String p) throws SQLException { return null; }
        @Override public PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(PrintWriter out) {}
        @Override public void setLoginTimeout(int s) {}
        @Override public int getLoginTimeout() { return 0; }
        @Override public Logger getParentLogger() { return null; }
        @Override public <T> T unwrap(Class<T> i) { return null; }
        @Override public boolean isWrapperFor(Class<?> i) { return false; }
    }
    class FakeAuctionDAO extends AuctionDAO {
        private Auction sampleAuction;
        private String updatedStatus;
        public FakeAuctionDAO(DataSource ds){
            super(ds);
        }
        public void setSampleAuction(Auction sampleAuction){
            this.sampleAuction =sampleAuction;
        }
        public String getUpdatedStatus(){
            return updatedStatus;
        }
        @Override
        public Auction findById(int id){
            return sampleAuction;
        }
        @Override
        public boolean updateStatus(int a, String b){
            this.updatedStatus = b;
            return true;
        }
        @Override
        public Auction create(Auction sampleAuction){
            return sampleAuction;
        }
        @Override
        public void deleteAuction(int id) {}
    }
    class FakeItemDAO extends ItemDAO{
        private Item sampleItem;
        public FakeItemDAO(DataSource ds){
            super(ds);
        }
        public void setSampleItem(Item item){
            this.sampleItem = item;
        }
        @Override
        public Item findById(int id){
            return sampleItem;
        }
    }
    class FakeUserDAO extends UserDAO{
        public FakeUserDAO(DataSource ds){
            super(ds);
        }
    }
    class FakeBidDAO extends BidDAO{
        public FakeBidDAO(DataSource ds){
            super(ds);
        }
        @Override
        public List<Bid> getBidsByAuction(int id){
            return new ArrayList<>();
        }
        @Override
        public void deleteByAuctionId(int auctionId) {
        }
        @Override
        public double getMaxBidByUser(int userId, int auctionId) {
            return 0.0;
        }
    }

}

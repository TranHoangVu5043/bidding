package Server.service.auction;

import Server.dao.auction.OrderDAO;
import Server.model.auction.items.Item;
import Server.model.auction.items.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderServiceTest {
    private OrderService orderService;
    private OFakeOrderDAO fakeOrderDAO;

    @BeforeEach
    public void setUp(){
        OSimpleFakeDataSource safeDataSource = new OSimpleFakeDataSource();
        fakeOrderDAO = new OFakeOrderDAO(safeDataSource);
        orderService = new OrderService(fakeOrderDAO);

    }
    @Test
    public void testCreatOrder_TotalIsNegative() {
        Order order = new Order(1, 1, 1, "ha", -120, "SOLD", LocalDateTime.now().minusDays(1));
        boolean or = orderService.createOrder(order);
        assertFalse(or, "Số tiền âm không tạo đơn được");
    }
    @Test
    public void testCreatOrder_NameIsEmpty() {
        Order order = new Order(1, 1, 1, null, 120, "SOLD", LocalDateTime.now().minusDays(1));
        Order order2 = new Order(1, 1, 1, "", -120, "SOLD", LocalDateTime.now().minusDays(1));

        boolean or = orderService.createOrder(order);
        boolean or2 = orderService.createOrder(order2);
        assertFalse(or, "Tên trống");
        assertFalse(or2, "Tên không hợp lệ");

    }
    @Test
    public void testCreatOrder_Success(){
        Order order = new Order(1, 1, 1, "ha", 120, "SOLD", LocalDateTime.now().minusDays(1));
        boolean or = orderService.createOrder(order);
        assertTrue(or, "Tạo đơn thành công");
    }
}
class OSimpleFakeDataSource implements javax.sql.DataSource {
    @Override public java.sql.Connection getConnection() { return null; }
    @Override public java.sql.Connection getConnection(String u, String p) { return null; }
    @Override public java.io.PrintWriter getLogWriter() { return null; }
    @Override public void setLogWriter(java.io.PrintWriter out) {}
    @Override public void setLoginTimeout(int s) {}
    @Override public int getLoginTimeout() { return 0; }
    @Override public java.util.logging.Logger getParentLogger() { return null; }
    @Override public <T> T unwrap(Class<T> i) { return null; }
    @Override public boolean isWrapperFor(Class<?> i) { return false; }
}
class OFakeOrderDAO extends OrderDAO {
    public OFakeOrderDAO(DataSource ds) { super(ds); }
    @Override public void create(Order order) {}
}

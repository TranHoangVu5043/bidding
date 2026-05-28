package Server.service.auction;

import Server.dao.auction.ItemDAO;
import Server.model.auction.items.Art;
import Server.model.auction.items.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class ItemServiceTest {
    private ItemService itemService;
    private FakeItemDAO fakeItemDAO;
    @BeforeEach
    public void setUp() {
        SimpleFakeDataSource safeDataSource = new SimpleFakeDataSource();
        fakeItemDAO = new FakeItemDAO(safeDataSource);
        itemService = new ItemService(fakeItemDAO);

    }
    @Test
    public void testAddItem(){
        Item fakeItem = new Art(1, null, "Đẹp", 3, "a", null, 5, 5);
        Item fakeItem1= new Art(1, " ", "Đẹp", 3, "a", null, 5, 5);

        Item item = itemService.addItem(fakeItem);
        Item item1 = itemService.addItem(fakeItem1);

        assertNull(item, "Trống tên");
        assertNull(item1, "Tên không có ký tự");
    }
    @Test
    public void testAddItem_Success() {
        Item fake = new Art(1, "null", "Đẹp", 3, "a", null, 5, 5);
        Item item = itemService.addItem(fake);
        assertNotNull( item, "Hợp lệ");
        assertEquals("null", item.getName(), "Tên giống nhau");
    }
    @Test
    public void testItemDoesNotExits(){
        fakeItemDAO.setSampleItem(null);
        assertFalse(itemService.deleteItem(1), "Xóa sản phẩm không tồn tại");
        Item item = new Art(1, null, "Đẹp", 3, "a", null, 5, 5);

        assertFalse(itemService.updateItem(item), "Sản phẩm không tồn tại");

        assertNull(itemService.getItemDetail(1), "Không tồn tại sản phẩm");

        assertFalse(itemService.canModifyItem(1,1), "Không tồn tại");
    }
    @Test
    public void testItemExits(){
        Item item = new Art(1, "null", "Đẹp", 3, "a", null, 5, 5);
        fakeItemDAO.setSampleItem(item);
        assertTrue(itemService.updateItem(item), "Sản phẩm tồn tại");

        assertNotNull(itemService.getItemDetail(1), "Tồn tại sản phẩm");
        assertEquals(1, itemService.getItemDetail(1).getId());

        assertTrue(itemService.canModifyItem(1,3), "Tồn tại");
        assertFalse(itemService.canModifyItem(1, 2), "Sai chủ sở hữu");
    }

}
class ForItemSimpleFakeDataSource implements javax.sql.DataSource {
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
class FakeItemDAO extends ItemDAO {
    private Item sampleItem;
    public FakeItemDAO(DataSource ds) { super(ds); }
    public void setSampleItem(Item item) { this.sampleItem = item; }

    @Override public Item findById(int id) { return this.sampleItem; }
    @Override public Item create(Item item) { return item; }
    @Override public void delete(int id) {}
    @Override public void update(Item item) {}
}
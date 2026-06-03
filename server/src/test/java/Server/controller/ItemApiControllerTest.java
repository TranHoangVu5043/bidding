package Server.controller;

import Server.dao.auction.ItemDAO;
import Server.model.auction.items.Art;
import Server.model.auction.items.Item;
import Server.model.users.Bidder;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.ItemService;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

public class ItemApiControllerTest {

    private ItemService itemService;
    private ItemApiController controller;
    private RequestWrapper req;
    private ResponseWrapper res;

    @BeforeEach
    public void setUp() {
        // Mock cac thanh phan phu thuoc
        itemService = mock(ItemService.class);
        req = mock(RequestWrapper.class);
        res = mock(ResponseWrapper.class);
        Gson gson = new Gson();

        // Khoi tao controller can test voi service gia lap
        controller = new ItemApiController(itemService);
    }

    @Test
    public void testGetMyItems_Unauthorized() {
        // 1. Giả lập: Request không có user đăng nhập (trả về null)
        when(req.getUser()).thenReturn(null);

        // 2. Hành động: Gọi hàm cần test
        controller.getMyItems(req, res);

        // 3. Kiểm tra: Controller phải báo lỗi 401 Unauthorized
        verify(res).error(401, "Unauthorized");
        verifyNoInteractions(itemService); // Đảm bảo không đụng gì tới service
    }

    @Test
    public void testGetMyItems_Success() {
        // 1. Chuẩn bị dữ liệu mẫu
        User fakeUser = new Bidder(10, "john", "12", "12@g.c", 100);
        Item item1 = new Art(1, "Tranh", "Đẹp", 10, "ART", "NEW", 100.0, 1);
        List<Item> mockList = List.of(item1);

        // 2. Giả lập hành vi
        when(req.getUser()).thenReturn(fakeUser);
        when(itemService.getItemsByOwner(10)).thenReturn(mockList);

        // 3. Hành động
        controller.getMyItems(req, res);

        // 4. Kiểm tra: Controller phải gửi mã 200 về cho client
        verify(res).sendJson(eq(200), anyString());
    }

    @Test
    public void testGetItem_Success() throws Exception{
        // 1. Giả lập: Client gửi Body lên dưới dạng JSON { "itemId": 5 }
        String jsonBody = "{\"itemId\": 5}";
        when(req.getBody()).thenReturn(jsonBody);

        // 2. Giả lập: Service tìm thấy item tương ứng
        Item mockItem = new Item(5, "Laptop", "Mạnh", 2, "ELECTRONICS", "USED", 500.0, 1) {};
        when(itemService.getItemDetail(5)).thenReturn(mockItem);

        // 3. Hành động
        controller.getItem(req, res);

        // 4. Kiểm tra: Phải trả về mã 200 thành công
        verify(res).sendJson(eq(200), anyString());
    }

    @Test
    public void testGetItem_NotFound() throws Exception{
        // Giả lập: Client tìm item số 999 nhưng service trả về null
        String jsonBody = "{\"itemId\": 999}";
        when(req.getBody()).thenReturn(jsonBody);
        when(itemService.getItemDetail(999)).thenReturn(null);

        controller.getItem(req, res);

        // Kiểm tra xem controller có trả về lỗi 404 không
        verify(res).error(404, "Item not found");
    }

    @Test
    public void testCreateItem_Forbidden_If_Not_Seller() {

        User fakeUser = new Bidder(10, "john", "12", "12@g.c", 100);

        when(req.getUser()).thenReturn(fakeUser);

        // 2. Hành động
        controller.createItem(req, res);

        // 3. Kiểm tra: Phải báo lỗi 403 Forbidden vì người mua không được tạo item
        verify(res).error(403, "Only sellers can add items");
    }

    @Test
    public void testDeleteItem_Success() throws Exception{
        Item item1 = new Art(1, "Tranh", "Đẹp", 10, "ART", "NEW", 100.0, 1);
        when(itemService.getItemDetail(1)).thenReturn(item1);
        // 1. Giả lập: User hợp lệ, gửi request xóa itemId = 1
        User fakeUser = new Bidder(10, "john", "12", "12@g.c", 100);
        when(req.getUser()).thenReturn(fakeUser);
        when(req.getBody()).thenReturn("{\"itemId\": 1}");

        // Giả lập: User này có quyền sửa món đồ và service xóa thành công (true)
        when(itemService.canModifyItem(1, 10)).thenReturn(true);
        when(itemService.deleteItem(1)).thenReturn(true);

        // 2. Hành động
        controller.deleteItem(req, res);

        // 3. Kiểm tra: Phải trả về mã 200 báo xóa thành công
        verify(res).sendJson(eq(200), anyString());
    }
}
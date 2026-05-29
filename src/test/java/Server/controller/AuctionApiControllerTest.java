package Server.controller;

import Server.model.auction.Auction;
import Server.model.users.Bidder;
import Server.model.users.Seller;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.AuctionService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuctionApiControllerTest {
    private AuctionApiController auctionApiController;

    @Mock private AuctionService auctionService;
    @Mock private UserService    userService;
    @Mock private ItemService    itemService;
    @Mock private RequestWrapper requestWrapper;
    @Mock private ResponseWrapper responseWrapper;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        auctionApiController = new AuctionApiController(auctionService, userService, itemService);
    }
    @Test
    void createAuction_Unauthorized_Returns41() {
        when(requestWrapper.getUser()).thenReturn(null);
        auctionApiController.createAuction(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Unauthorized");
    }

    @Test
    void createAuction_NotSeller_Returns403() {
        User regularUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(regularUser);
        auctionApiController.createAuction(requestWrapper, responseWrapper);
        verify(responseWrapper).error(403, "Only sellers can create auctions");
    }

    @Test
    void createAuction_MissingFields_Returns400() throws Exception{
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        // Gửi body trống rỗng thiếu trường bắt buộc
        when(requestWrapper.getBody()).thenReturn("{}");
        auctionApiController.createAuction(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Missing required fields: itemId, startingPrice, endTime");
    }

    @Test
    void createAuction_EndTimeBeforeStartTime_Returns400() throws Exception {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("itemId", 10);
        bodyJson.addProperty("startingPrice", 500.0);
        bodyJson.addProperty("startTime", "2026-06-01T12:00:00");
        bodyJson.addProperty("endTime", "2026-06-01T10:00:00"); // End trước Start
        when(requestWrapper.getBody()).thenReturn(bodyJson.toString());

        auctionApiController.createAuction(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "endTime must be after startTime");
    }

    @Test
    void createAuction_ServiceReturnsNull_Returns400()  throws  Exception {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);

        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("itemId", 10);
        bodyJson.addProperty("startingPrice", 500.0);
        bodyJson.addProperty("endTime", "2026-06-01T12:00:00");
        when(requestWrapper.getBody()).thenReturn(bodyJson.toString());

        when(auctionService.createAuction(any(Auction.class), eq(seller.getId()))).thenReturn(null);
        auctionApiController.createAuction(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Failed to create auction — item not found or not owned by you");
    }

    @Test
    void createAuction_ValidRequest_Returns201() throws Exception {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);

        JsonObject bodyJson = new JsonObject();
        bodyJson.addProperty("itemId", 10);
        bodyJson.addProperty("startingPrice", 500.0);
        bodyJson.addProperty("endTime", "2026-06-01T12:00:00");
        when(requestWrapper.getBody()).thenReturn(bodyJson.toString());

        Auction mockCreated = new Auction(1, 10, 1, 500.0, 500.0, LocalDateTime.now(), LocalDateTime.parse("2026-06-01T12:00:00"), "UPCOMING");
        when(auctionService.createAuction(any(Auction.class), eq(seller.getId()))).thenReturn(mockCreated);

        auctionApiController.createAuction(requestWrapper, responseWrapper);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseWrapper).sendJson(eq(201), jsonCaptor.capture());

        JsonObject responseJson = gson.fromJson(jsonCaptor.getValue(), JsonObject.class);
        assertEquals(201, responseJson.get("status").getAsInt());
        assertEquals("Auction created", responseJson.get("message").getAsString());
    }
    @Test
    void getMyAuctions_Unauthorized_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);

        auctionApiController.getMyAuctions(requestWrapper, responseWrapper);

        verify(responseWrapper).error(401, "Unauthorized");
    }

    @Test
    void getMyAuctions_Success_Returns200WithList() {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        when(auctionService.getAuctionsByOwner(seller.getId())).thenReturn(Collections.emptyList());

        auctionApiController.getMyAuctions(requestWrapper, responseWrapper);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseWrapper).sendJson(eq(200), jsonCaptor.capture());

        JsonObject responseJson = gson.fromJson(jsonCaptor.getValue(), JsonObject.class);
        assertEquals(200, responseJson.get("status").getAsInt());
        assertTrue(responseJson.get("data").isJsonArray());
    }

    @Test
    void getAllAuctions_FiltersOutCancelled_Returns200() {
        Auction active = new Auction(1, 10, 1, 100.0, 100.0, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "ACTIVE");
        Auction cancelled = new Auction(2, 11, 1, 200.0, 200.0, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "CANCELLED");

        when(auctionService.getAllAuctions()).thenReturn(List.of(active, cancelled));
        auctionApiController.getAllAuctions(requestWrapper, responseWrapper);

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(responseWrapper).sendJson(eq(200), jsonCaptor.capture());

        JsonObject responseJson = gson.fromJson(jsonCaptor.getValue(), JsonObject.class);
        // Kiểm tra xem data trả ra mảng chỉ chứa 1 phần tử (vì đã filter mất bản ghi CANCELLED)
        assertEquals(1, responseJson.get("data").getAsJsonArray().size());
    }

    // --- TEST CASE FOR: getAuction ---

    @Test
    void getAuction_InvalidId_Returns400() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 0}");

        auctionApiController.getAuction(requestWrapper, responseWrapper);

        verify(responseWrapper).error(400, "Missing required field: auctionId");
    }

    @Test
    void getAuction_NotFound_Returns404() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 999}");
        when(auctionService.getAuction(999)).thenReturn(null);

        auctionApiController.getAuction(requestWrapper, responseWrapper);

        verify(responseWrapper).error(404, "Auction not found");
    }

    @Test
    void getAuction_Success_Returns200() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 1}");
        Auction mockAuction = new Auction(1, 10, 1, 100.0, 100.0, LocalDateTime.now(), LocalDateTime.now().plusHours(1), "ACTIVE");
        when(auctionService.getAuction(1)).thenReturn(mockAuction);

        auctionApiController.getAuction(requestWrapper, responseWrapper);

        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void cancelAuction_Unauthorized_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);

        auctionApiController.cancelAuction(requestWrapper, responseWrapper);

        verify(responseWrapper).error(401, "Unauthorized");
    }

    @Test
    void cancelAuction_FailedToCancel_Returns400() throws Exception{
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 1}");
        // Giả lập huỷ thất bại
        when(auctionService.cancelAuction(1, seller.getId())).thenReturn(false);

        auctionApiController.cancelAuction(requestWrapper, responseWrapper);

        verify(responseWrapper).error(400, "Cannot cancel auction — it may not exist, already be finished, or not belong to you");
    }

    @Test
    void cancelAuction_Success_Returns200() throws Exception {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 1}");
        when(auctionService.cancelAuction(1, seller.getId())).thenReturn(true);

        auctionApiController.cancelAuction(requestWrapper, responseWrapper);

        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void refreshStatus_Success_CallsService() throws Exception {
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 5}");
        auctionApiController.refreshStatus(requestWrapper, responseWrapper);
        verify(auctionService).refreshAuctionStatus(5);
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void refreshStatus_Exception_Returns500() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 5}");
        doThrow(new RuntimeException("Database down")).when(auctionService).refreshAuctionStatus(5);

        auctionApiController.refreshStatus(requestWrapper, responseWrapper);

        verify(responseWrapper).error(500, "Server error: Database down");
    }
}
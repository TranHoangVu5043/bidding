package Server.controller;

import Server.model.auction.Bid;
import Server.model.users.Bidder;
import Server.model.users.Seller;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.dao.auction.BidDAO;
import Server.service.auction.AutoBidConfigService;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BidApiControllerTest {

    private BidApiController bidApiController;

    @Mock private BiddingService biddingService;
    @Mock private UserService userService;
    @Mock private ItemService itemService;
    @Mock private BidDAO bidDAO;
    @Mock private RequestWrapper requestWrapper;
    @Mock private ResponseWrapper responseWrapper;

    @Mock private AutoBidConfigService autoBidConfigService;

    @BeforeEach
    void setUp() {
        lenient().when(biddingService.getBidDAO()).thenReturn(bidDAO);
        bidApiController = new BidApiController(biddingService, userService, itemService);
    }

    // --- TEST CASE FOR: placeBid ---

    @Test
    void placeBid_Unauthorized_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);
        bidApiController.placeBid(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Unauthorized");
    }

    @Test
    void placeBid_NotBidder_Returns403() {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        bidApiController.placeBid(requestWrapper, responseWrapper);
        verify(responseWrapper).error(403, "Only bidders can place bids");
    }

    @Test
    void placeBid_MissingFields_Returns400() throws Exception{
        User bidder = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(bidder);
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 0, \"amount\": 0}"); // Sai dữ liệu
        bidApiController.placeBid(requestWrapper, responseWrapper);

        verify(responseWrapper).error(400, "Missing required fields: auctionId, amount");
    }

    @Test
    void placeBid_RuntimeExceptionFromService_Returns400() throws Exception{
        User bidder = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(bidder);
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 1, \"amount\": 150.0}");

        doThrow(new RuntimeException("Bid amount must be greater than current price"))
                .when(biddingService).placeBid(1, 1, 150.0);
        bidApiController.placeBid(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Bid amount must be greater than current price");
    }

    @Test
    void placeBid_Success_Returns201() throws Exception{
        User bidder = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(bidder);
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 1, \"amount\": 200.0}");

        bidApiController.placeBid(requestWrapper, responseWrapper);
        verify(biddingService).placeBid(1, 1, 200.0);
        verify(responseWrapper).sendJson(eq(201), anyString());
    }

    // --- TEST CASE FOR: getBidHistory ---

    @Test
    void getBidHistory_InvalidAuctionId_Returns400() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": -5}");
        bidApiController.getBidHistory(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Missing required field: auctionId");
    }

    @Test
    void getBidHistory_Success_Returns200WithData() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"auctionId\": 1}");
        Bid mockBid = new Bid(1, 1, 2, 150.0, LocalDateTime.now());
        when(biddingService.getBidHistory(1)).thenReturn(List.of(mockBid));

        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        bidApiController.getBidHistory(requestWrapper, responseWrapper);
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    // --- TEST CASE FOR: getMyBiddingAuctions ---

    @Test
    void getMyBiddingAuctions_Unauthorized_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);
        bidApiController.getMyBiddingAuctions(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Unauthorized");
    }

    @Test
    void getMyBiddingAuctions_Success_Returns200() {
        User bidder = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(bidder);
        when(biddingService.getAuctionsForBidder(1)).thenReturn(Collections.emptyList());
        bidApiController.getMyBiddingAuctions(requestWrapper, responseWrapper);
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    // --- TEST CASE FOR: registerAutoBid ---

    @Test
    void registerAutoBid_NotBidder_Returns403() {
        User seller = new Seller(1, "store", "123", "s@g.c", "HanSto ", 1200);
        when(requestWrapper.getUser()).thenReturn(seller);
        bidApiController.registerAutoBid(requestWrapper, responseWrapper);
        verify(responseWrapper).error(403, "Only bidders can register auto-bids");
    }

    @Test
    void registerAutoBid_Success_CallsSubService() throws Exception{
        User bidder = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(bidder);

        JsonObject body = new JsonObject();
        body.addProperty("auctionId", 1);
        body.addProperty("maxBid", 500.0);
        body.addProperty("increment", 10.0);
        when(requestWrapper.getBody()).thenReturn(body.toString());

        when(biddingService.getAutoBidConfigService()).thenReturn(autoBidConfigService);

        bidApiController.registerAutoBid(requestWrapper, responseWrapper);

        verify(autoBidConfigService).registerAutoBid(1, 1, 500.0, 10.0);
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    // --- TEST CASE FOR: getMyBidHistory ---

    @Test
    void getMyBidHistory_Exception_Returns500() {
        User bidder = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(bidder);

        when(biddingService.getBidHistoryForUser(1)).thenThrow(new RuntimeException("DB Connection reset"));

        bidApiController.getMyBidHistory(requestWrapper, responseWrapper);

        verify(responseWrapper).error(500, "Server error: DB Connection reset");
    }
}
package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.dto.requests.AuctionIdRequest;
import Server.dto.requests.AutoBidRequest;
import Server.dto.requests.PlaceBidRequest;
import Server.dto.responses.AuctionDTO;
import Server.dto.responses.BidDTO;
import Server.dto.responses.BidHistoryDTO;
import Server.model.auction.Auction;
import Server.model.auction.Bid;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Server.model.auction.items.Item;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BidApiController {

    private static final Logger log = LoggerFactory.getLogger(BidApiController.class);

    private final BiddingService biddingService;
    private final UserService    userService;
    private final ItemService    itemService;
    private final Gson gson;

    public BidApiController(BiddingService biddingService, UserService userService, ItemService itemService) {
        this.biddingService = biddingService;
        this.userService    = userService;
        this.itemService    = itemService;
        this.gson = new Gson();
    }

    // POST /api/bids/place
    // Body: { "auctionId": 1, "amount": 150.0 }
    public void placeBid(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            if (!"bidder".equalsIgnoreCase(user.getRole())) {
                res.error(403, "Only bidders can place bids");
                return;
            }

            PlaceBidRequest body = gson.fromJson(req.getBody(), PlaceBidRequest.class);
            if (body == null || body.auctionId <= 0 || body.amount <= 0) {
                res.error(400, "Missing required fields: auctionId, amount");
                return;
            }

            double newBalance = biddingService.placeBid(user.getId(), body.auctionId, body.amount);
            res.sendJson(201, gson.toJson(new ApiResponse<>(201, "Đặt giá thành công!", newBalance)));

        } catch (RuntimeException e) {
            res.error(400, e.getMessage());
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/bids/history
    // Body: { "auctionId": 1 }
    public void getBidHistory(RequestWrapper req, ResponseWrapper res) {
        try {
            AuctionIdRequest body = gson.fromJson(req.getBody(), AuctionIdRequest.class);
            if (body == null || body.auctionId <= 0) {
                res.error(400, "Missing required field: auctionId");
                return;
            }

            List<Bid> bids = biddingService.getBidHistory(body.auctionId);
            Map<Integer, User> userMap = userService.getAllUsers().stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            List<BidDTO> dtos = bids.stream().map(b -> {
                User u = userMap.get(b.getUserId());
                String username = (u != null) ? u.getUsername() : "User#" + b.getUserId();
                return new BidDTO(b, username);
            }).toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));

        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // GET /api/bids/my-auctions
    public void getMyBiddingAuctions(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            List<Auction> auctions = biddingService.getAuctionsForBidder(user.getId());
            Map<Integer, User> sellerMap = userService.getAllUsers().stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            Map<Integer, Item> itemMap = itemService.getAllItems().stream()
                    .collect(Collectors.toMap(Item::getId, i -> i));
            List<AuctionDTO> dtos = auctions.stream()
                    .map(a -> new AuctionDTO(a, sellerMap.get(a.getOwnerId()), itemMap.get(a.getItemId())))
                    .toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));

        } catch (Exception e) {
            log.error("getMyBiddingAuctions failed for user#{}", req.getUser() != null ? req.getUser().getId() : "?", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/bids/autobid
    // Body: { "auctionId": 1, "maxBid": 500.0, "increment": 10.0 }
    public void registerAutoBid(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            if (!"bidder".equalsIgnoreCase(user.getRole())) {
                res.error(403, "Only bidders can register auto-bids");
                return;
            }

            AutoBidRequest body = gson.fromJson(req.getBody(), AutoBidRequest.class);
            if (body == null || body.auctionId <= 0 || body.maxBid <= 0 || body.increment <= 0) {
                res.error(400, "Missing required fields: auctionId, maxBid, increment");
                return;
            }

            biddingService.getAutoBidConfigService()
                    .registerAutoBid(body.auctionId, user.getId(), body.maxBid, body.increment);

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Auto-bid registered successfully", null)));

        } catch (RuntimeException e) {
            res.error(400, e.getMessage());
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // GET /api/bids/my-history
    // Returns finished auctions the user bid on with won/lost outcome
    public void getMyBidHistory(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            List<BiddingService.BidHistoryEntry> entries =
                    biddingService.getBidHistoryForUser(user.getId());

            Map<Integer, User> sellerMap = userService.getAllUsers().stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            Map<Integer, Item> itemMap = itemService.getAllItems().stream()
                    .collect(Collectors.toMap(Item::getId, i -> i));
            List<BidHistoryDTO> dtos = entries.stream()
                    .map(e -> new BidHistoryDTO(e,
                            sellerMap.get(e.auction().getOwnerId()),
                            itemMap.get(e.auction().getItemId())))
                    .toList();

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));

        } catch (Exception e) {
            log.error("getMyBidHistory failed for user#{}", req.getUser() != null ? req.getUser().getId() : "?", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

}

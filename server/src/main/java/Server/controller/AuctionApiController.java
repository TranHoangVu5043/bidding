package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.dto.requests.AuctionIdRequest;
import Server.dto.requests.CreateAuctionRequest;
import Server.dto.responses.AuctionDTO;
import Server.model.auction.Auction;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.dao.auction.BidDAO;
import Server.service.auction.AuctionService;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Server.model.auction.items.Item;
import Server.model.users.User;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AuctionApiController {

    private static final Logger log = LoggerFactory.getLogger(AuctionApiController.class);

    private final AuctionService auctionService;
    private final UserService    userService;
    private final ItemService    itemService;
    private final BidDAO         bidDAO;
    private final Gson gson;

    public AuctionApiController(AuctionService auctionService, UserService userService, ItemService itemService, BiddingService biddingService) {
        this.auctionService = auctionService;
        this.userService    = userService;
        this.itemService    = itemService;
        this.bidDAO         = biddingService.getBidDAO();
        this.gson = new Gson();
    }

    // POST /api/auctions/create
    // Body: { "itemId": 1, "startingPrice": 100.0, "startTime": "2026-06-01T10:00:00", "endTime": "2026-06-01T12:00:00" }
    public void createAuction(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            if (!"seller".equalsIgnoreCase(user.getRole())) {
                res.error(403, "Only sellers can create auctions");
                return;
            }

            CreateAuctionRequest body = gson.fromJson(req.getBody(), CreateAuctionRequest.class);
            if (body == null || body.itemId <= 0 || body.startingPrice <= 0 || body.endTime == null) {
                res.error(400, "Missing required fields: itemId, startingPrice, endTime");
                return;
            }

            LocalDateTime startTime = body.startTime != null
                    ? LocalDateTime.parse(body.startTime)
                    : LocalDateTime.now(ZoneOffset.UTC);
            LocalDateTime endTime = LocalDateTime.parse(body.endTime);

            if (!endTime.isAfter(startTime)) {
                res.error(400, "endTime must be after startTime");
                return;
            }

            Auction auction = new Auction(
                    0, body.itemId, user.getId(),
                    body.startingPrice, body.startingPrice,
                    startTime, endTime, "UPCOMING"
            );

            Auction created = auctionService.createAuction(auction, user.getId());
            if (created == null) {
                res.error(400, "Failed to create auction — item not found or not owned by you");
                return;
            }

            res.sendJson(201, gson.toJson(new ApiResponse<>(201, "Auction created", new AuctionDTO(created, userService, itemService))));

        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // GET /api/auctions/mine
    // Returns only auctions owned by the logged-in seller
    public void getMyAuctions(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            List<Auction> auctions = auctionService.getAuctionsByOwner(user.getId());
            Map<Integer, User> userMap = userService.getAllUsers().stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            Map<Integer, Item> itemMap = itemService.getAllItems().stream()
                    .collect(Collectors.toMap(Item::getId, i -> i));
            List<Integer> auctionIds = auctions.stream().map(Auction::getId).toList();
            Map<Integer, Integer> highestBidderIds = bidDAO.findHighestBidders(auctionIds);
            List<AuctionDTO> dtos = auctions.stream()
                    .map(a -> {
                        Integer bidderId = highestBidderIds.get(a.getId());
                        return new AuctionDTO(a, user, itemMap.get(a.getItemId()),
                                bidderId != null ? userMap.get(bidderId) : null);
                    })
                    .toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // GET /api/auctions
    // No body needed — returns all auctions
    public void getAllAuctions(RequestWrapper req, ResponseWrapper res) {
        try {
            List<Auction> auctions = auctionService.getAllAuctions();
            Map<Integer, User> userMap = userService.getAllUsers().stream()
                    .collect(Collectors.toMap(User::getId, u -> u));
            Map<Integer, Item> itemMap = itemService.getAllItems().stream()
                    .collect(Collectors.toMap(Item::getId, i -> i));
            List<Integer> auctionIds = auctions.stream().map(Auction::getId).toList();
            Map<Integer, Integer> highestBidderIds = bidDAO.findHighestBidders(auctionIds);
            List<AuctionDTO> dtos = auctions.stream()
                    .map(a -> {
                        Integer bidderId = highestBidderIds.get(a.getId());
                        return new AuctionDTO(a, userMap.get(a.getOwnerId()), itemMap.get(a.getItemId()), bidderId != null ? userMap.get(bidderId) : null);
                    })
                    .toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/auctions/get
    // Body: { "auctionId": 1 }
    public void getAuction(RequestWrapper req, ResponseWrapper res) {
        try {
            AuctionIdRequest body = gson.fromJson(req.getBody(), AuctionIdRequest.class);
            if (body == null || body.auctionId <= 0) {
                res.error(400, "Missing required field: auctionId");
                return;
            }

            Auction auction = auctionService.getAuction(body.auctionId);
            if (auction == null) {
                res.error(404, "Auction not found");
                return;
            }

            User seller = userService.getUserById(auction.getOwnerId());
            Item item   = itemService.getItemDetail(auction.getItemId());
            Integer bidderId = bidDAO.findHighestBidder(auction.getId());
            User highestBidder = bidderId != null ? userService.getUserById(bidderId) : null;
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", new AuctionDTO(auction, seller, item, highestBidder))));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/auctions/finish-early
    // Body: { "auctionId": 1 }
    public void finishEarly(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            if (!"seller".equalsIgnoreCase(user.getRole())) {
                res.error(403, "Only sellers can finish auctions early");
                return;
            }

            AuctionIdRequest body = gson.fromJson(req.getBody(), AuctionIdRequest.class);
            if (body == null || body.auctionId <= 0) {
                res.error(400, "Missing required field: auctionId");
                return;
            }

            boolean ok = auctionService.finishEarly(body.auctionId, user.getId());
            if (!ok) {
                res.error(400, "Cannot finish — auction must be ACTIVE and belong to you");
                return;
            }

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Phiên đấu giá đã kết thúc sớm", null)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/auctions/cancel
    // Body: { "auctionId": 1 }
    public void cancelAuction(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            AuctionIdRequest body = gson.fromJson(req.getBody(), AuctionIdRequest.class);
            if (body == null || body.auctionId <= 0) {
                res.error(400, "Missing required field: auctionId");
                return;
            }

            boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole());
            boolean canceled = auctionService.cancelAuction(body.auctionId, user.getId(), isAdmin);
            if (!canceled) {
                res.error(400, "Cannot cancel auction — it may not exist, already be finished, or not belong to you");
                return;
            }

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Auction canceled", null)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/auctions/refresh
    // Body: { "auctionId": 1 }
    public void refreshStatus(RequestWrapper req, ResponseWrapper res) {
        try {
            AuctionIdRequest body = gson.fromJson(req.getBody(), AuctionIdRequest.class);
            if (body == null || body.auctionId <= 0) {
                res.error(400, "Missing required field: auctionId");
                return;
            }

            auctionService.refreshAuctionStatus(body.auctionId);
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Status refreshed", null)));
        } catch (Exception e) {
            log.error("Unhandled exception", e);
            res.error(500, "Server error: " + e.getMessage());
        }
    }

}
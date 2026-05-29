package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.dto.requests.AuctionIdRequest;
import Server.dto.requests.CreateAuctionRequest;
import Server.dto.responses.AuctionDTO;
import Server.model.auction.Auction;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.AuctionService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionApiController {

    private final AuctionService auctionService;
    private final UserService    userService;
    private final ItemService    itemService;
    private final Gson gson;

    public AuctionApiController(AuctionService auctionService, UserService userService, ItemService itemService) {
        this.auctionService = auctionService;
        this.userService    = userService;
        this.itemService    = itemService;
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
                    : LocalDateTime.now();
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
            List<AuctionDTO> dtos = auctions.stream().map(a -> new AuctionDTO(a, userService, itemService)).toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // GET /api/auctions
    // No body needed — returns all auctions
    public void getAllAuctions(RequestWrapper req, ResponseWrapper res) {
        try {
            List<Auction> auctions = auctionService.getAllAuctions();
            List<AuctionDTO> dtos = auctions.stream().map(a -> new AuctionDTO(a, userService, itemService)).toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));
        } catch (Exception e) {
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

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", new AuctionDTO(auction, userService, itemService))));
        } catch (Exception e) {
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

            boolean canceled = auctionService.cancelAuction(body.auctionId, user.getId());
            if (!canceled) {
                res.error(400, "Cannot cancel auction — it may not exist, already be finished, or not belong to you");
                return;
            }

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Auction canceled", null)));
        } catch (Exception e) {
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
            res.error(500, "Server error: " + e.getMessage());
        }
    }

}
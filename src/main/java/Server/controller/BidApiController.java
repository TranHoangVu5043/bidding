package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.model.auction.Bid;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.BiddingService;
import Server.service.users.UserService;

import com.google.gson.Gson;

import java.util.List;

public class BidApiController {

    private final BiddingService biddingService;
    private final UserService userService;
    private final Gson gson;

    public BidApiController(BiddingService biddingService, UserService userService) {
        this.biddingService = biddingService;
        this.userService = userService;
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

            biddingService.placeBid(user.getId(), body.auctionId, body.amount);

            res.sendJson(201, gson.toJson(new ApiResponse<>(201, "Bid placed successfully", null)));

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
            List<BidDTO> dtos = bids.stream().map(b -> {
                User u = userService.getUserById(b.getUserId());
                String username = (u != null) ? u.getUsername() : "User#" + b.getUserId();
                return new BidDTO(b, username);
            }).toList();
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", dtos)));

        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    private static class PlaceBidRequest {
        int auctionId;
        double amount;
    }

    private static class AuctionIdRequest {
        int auctionId;
    }

    private static class BidDTO {
        int id, userId, auctionId;
        double amount;
        String createdAt;
        String username;

        BidDTO(Bid b, String username) {
            id = b.getId();
            userId = b.getUserId();
            auctionId = b.getAuctionId();
            amount = b.getAmount();
            createdAt = b.getCreatedAt() != null ? b.getCreatedAt().toString() : null;
            this.username = username;
        }
    }
}

package Client.networking.endpoints;

import Client.dto.requests.AuctionIdBody;
import Client.dto.requests.AutoBidBody;
import Client.dto.requests.PlaceBidBody;
import Client.model.auction.Auction;
import Client.model.auction.Bid;
import Client.model.auction.BidHistoryItem;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class BidApi {

    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ApiResponse<Double> placeBid(int auctionId, double amount) {
        try {
            String json = apiClient.post("/bids/place", new PlaceBidBody(auctionId, amount));
            return gson.fromJson(json, new TypeToken<ApiResponse<Double>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<List<Auction>> getMyBiddingAuctions() {
        try {
            String json = apiClient.get("/bids/my-auctions");
            return gson.fromJson(json, new TypeToken<ApiResponse<List<Auction>>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<List<BidHistoryItem>> getMyBidHistory() {
        try {
            String json = apiClient.get("/bids/my-history");
            return gson.fromJson(json, new TypeToken<ApiResponse<List<BidHistoryItem>>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<List<Bid>> getBidHistory(int auctionId) {
        try {
            String json = apiClient.post("/bids/history", new AuctionIdBody(auctionId));
            return gson.fromJson(json, new TypeToken<ApiResponse<List<Bid>>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> registerAutoBid(int auctionId, double maxBid, double increment) {
        try {
            String json = apiClient.post("/bids/autobid", new AutoBidBody(auctionId, maxBid, increment));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    private <T> ApiResponse<T> error(Exception e) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setStatus(500);
        r.setMessage(e.getMessage());
        return r;
    }

}
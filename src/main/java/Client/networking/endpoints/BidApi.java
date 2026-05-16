package Client.networking.endpoints;

import Client.model.Bid;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class BidApi {

    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ApiResponse<Void> placeBid(int auctionId, double amount) {
        try {
            String json = apiClient.post("/bids/place", new PlaceBody(auctionId, amount));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
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

    private <T> ApiResponse<T> error(Exception e) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setStatus(500);
        r.setMessage(e.getMessage());
        return r;
    }

    private static class PlaceBody {
        int auctionId;
        double amount;
        PlaceBody(int id, double amt) { this.auctionId = id; this.amount = amt; }
    }

    private static class AuctionIdBody {
        int auctionId;
        AuctionIdBody(int id) { this.auctionId = id; }
    }
}
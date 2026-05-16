package Client.networking.endpoints;

import Client.model.Auction;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class AuctionApi {

    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ApiResponse<List<Auction>> getAllAuctions() {
        try {
            String json = apiClient.get("/auctions");
            return gson.fromJson(json, new TypeToken<ApiResponse<List<Auction>>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Auction> getAuction(int auctionId) {
        try {
            String json = apiClient.post("/auctions/get", new IdBody(auctionId));
            return gson.fromJson(json, new TypeToken<ApiResponse<Auction>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> createAuction(int itemId, double startingPrice, String startTime, String endTime) {
        try {
            String json = apiClient.post("/auctions/create", new CreateBody(itemId, startingPrice, startTime, endTime));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> cancelAuction(int auctionId) {
        try {
            String json = apiClient.post("/auctions/cancel", new IdBody(auctionId));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> refreshStatus(int auctionId) {
        try {
            String json = apiClient.post("/auctions/refresh", new IdBody(auctionId));
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

    private static class IdBody {
        int auctionId;
        IdBody(int id) { this.auctionId = id; }
    }

    private static class CreateBody {
        int itemId;
        double startingPrice;
        String startTime;
        String endTime;
        CreateBody(int i, double p, String s, String e) {
            itemId = i; startingPrice = p; startTime = s; endTime = e;
        }
    }
}
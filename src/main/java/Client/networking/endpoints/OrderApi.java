package Client.networking.endpoints;

import Client.model.auction.Order;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class OrderApi {
    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ApiResponse<List<Order>> getRecentOrders() {
        try {
            String responseJson = apiClient.get("/orders/recent");

            return gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<List<Order>>>() {}.getType()
            );

        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<List<Order>> response = new ApiResponse<>();
            response.setStatus(500);
            response.setMessage("Lỗi kết nối API đơn hàng: " + e.getMessage());
            return response;
        }
    }
    public ApiResponse<List<Order>> getAllOrders() {
        try {
            String responseJson = apiClient.get("/orders/all");
            return gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<List<Order>>>() {}.getType()
            );
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<List<Order>> response = new ApiResponse<>();
            response.setStatus(500);
            response.setMessage("Lỗi kết nối API: " + e.getMessage());
            return response;
        }
    }
}

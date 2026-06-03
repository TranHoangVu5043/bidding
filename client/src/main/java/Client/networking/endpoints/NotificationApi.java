package Client.networking.endpoints;

import Client.model.Notification;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class NotificationApi {
    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ApiResponse<List<Notification>> getNotifications() {
        try {
            String json = apiClient.get("/notifications");
            return gson.fromJson(json, new TypeToken<ApiResponse<List<Notification>>>(){}.getType());
        } catch (Exception e) { return error(e); }
    }

    public ApiResponse<Void> markAllRead() {
        try {
            String json = apiClient.post("/notifications/read-all", new Object());
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>(){}.getType());
        } catch (Exception e) { return error(e); }
    }

    private <T> ApiResponse<T> error(Exception e) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setStatus(500);
        r.setMessage(e.getMessage());
        return r;
    }
}

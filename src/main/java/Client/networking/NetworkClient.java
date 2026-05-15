package Client.networking;

/*import Server.controller.UserApiController;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class NetworkClient {
    private static NetworkClient instance;
    private final HttpClient httpClient;
    private final Gson gson;

    // IP của Server, nếu chạy cùng máy thì để localhost
    private final String BASE_URL = "http://localhost:8080/api/user";

    private NetworkClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    /**
     * Hàm dùng chung để gửi dữ liệu lên Server
     */
    /*public UserApiController.ApiResponse post(String endpoint, Object data) {
        try {
            String jsonStr = gson.toJson(data);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonStr))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Ép kiểu JSON nhận được về ApiResponse
            return gson.fromJson(response.body(), UserApiController.ApiResponse.class);

        } catch (java.net.ConnectException e) {
            return new UserApiController.ApiResponse(503, "Lỗi: Không thể kết nối tới Server. Hãy kiểm tra xem Server đã bật chưa!", null);
        } catch (Exception e) {
            return new UserApiController.ApiResponse(500, "Lỗi kết nối: " + e.getMessage(), null);
        }
    }
}*/
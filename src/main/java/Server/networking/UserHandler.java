package Server.networking;

import Server.controller.UserApiController;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class UserHandler implements HttpHandler {
    private final UserApiController controller = new UserApiController();
    private final Gson gson = new Gson();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Thiết lập Header để tránh lỗi CORS và định dạng JSON
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Content-Type", "application/json");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String responseContent = "";

        // Chỉ xử lý POST cho Login và Register
        if ("POST".equalsIgnoreCase(method)) {
            try {
                // 1. Đọc dữ liệu thô từ Stream
                String jsonBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

                // 2. Điều hướng dựa trên đường dẫn (Endpoint)
                if (path.endsWith("/login")) {
                    responseContent = controller.handleLogin(jsonBody);
                } else if (path.endsWith("/register")) {
                    responseContent = controller.handleRegister(jsonBody);
                }
            } catch (Exception e) {
                responseContent = "{\"status\":500, \"message\":\"Lỗi Server: " + e.getMessage() + "\"}";
            }
        } else {
            responseContent = "{\"status\":405, \"message\":\"Phương thức không hỗ trợ\"}";
        }

        // 3. Gửi phản hồi về Client
        byte[] responseBytes = responseContent.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}
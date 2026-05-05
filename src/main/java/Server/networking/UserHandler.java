package Server.networking;

import Server.controller.UserApiController;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class UserHandler implements HttpHandler {
    // Kết nối trực tiếp với Controller đã làm
    private final UserApiController controller = new UserApiController();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Thiết lập để chấp nhận các yêu cầu từ Client (CORS)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String response = "";

        // Chỉ xử lý nếu là phương thức POST (cho Login/Register)
        if ("POST".equalsIgnoreCase(method)) {
            // Đọc dữ liệu JSON từ gói tin HTTP gửi đến
            String jsonBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            // Logic điều hướng (Routing)
            if (path.endsWith("/login")) {
                response = controller.handleLogin(jsonBody);
            } else if (path.endsWith("/register")) {
                response = controller.handleRegister(jsonBody);
            } else if (path.endsWith("/loginwtoken")) {
                // response = controller.handleLoginWithToken(jsonBody);
            }
        } else {
            response = "{\"status\":405, \"message\":\"Method Not Allowed\"}";
        }

        // Gửi phản hồi về cho Client qua HTTP
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
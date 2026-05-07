package Server.controller;

import Server.model.ApiResponse;
import Server.model.users.User;
import Server.service.users.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class UserApiController implements HttpHandler {

    private final UserService userService;

    public UserApiController(UserService userService) {
        this.userService = userService;
    }

    public String handleLogin(String jsonRequest) {
        try {

            User loginData = gson.fromJson(jsonRequest, User.class);

            String token = userService.login(loginData.getUsername(), loginData.getPassword());
            return gson.toJson(new ApiResponse(200, "Đăng nhập thành công !", token));
        } catch (Exception e) {

            return gson.toJson(new ApiResponse(401, "Lỗi: " + e.getMessage(), null));
        }
    }

    public String handleRegister(String jsonRequest) {
        try {
            User newUser = gson.fromJson(jsonRequest, User.class);
            userService.register(newUser.getUsername(), newUser.getPassword());
            return gson.toJson(new ApiResponse(201, "Đăng ký tài khoản thành công!", null));
        } catch (Exception e) {
            return gson.toJson(new ApiResponse(400, "Đăng ký thất bại: " + e.getMessage(), null));
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        //exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

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

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, responseBytes.length);

        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }
}
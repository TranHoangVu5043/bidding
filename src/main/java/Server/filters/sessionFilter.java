package Server.filters;

import Server.controller.responseObjects.ApiResponse;
import Server.service.users.UserService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import com.google.gson.Gson;
import java.io.IOException;
import java.util.Set;

public class sessionFilter extends Filter {

    private final UserService userService;
    private final Gson gson;

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/users/login",
            "/api/users/register"
    );

    public sessionFilter(UserService userService) {
        this.userService = userService;
        this.gson        = new Gson();
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (PUBLIC_PATHS.contains(path)) {
            chain.doFilter(exchange);
            return;
        }

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendJsonError(exchange, 401, "Token không hợp lệ hoặc bị thiếu.");
            return;
        }

        String token = authHeader.substring(7).trim();

        if (token.isEmpty()) {
            sendJsonError(exchange, 401, "Token không được để trống.");
            return;
        }

        var user = userService.authenticate(token);

        if (user == null) {
            sendJsonError(exchange, 401, "Token đã hết hạn hoặc không tồn tại. Vui lòng đăng nhập lại.");
            return;
        }

        exchange.setAttribute("user", user);
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Session Auth Filter";
    }

    private void sendJsonError(HttpExchange exchange, int status, String message) throws IOException {
        ApiResponse<Object> body = new ApiResponse<>(status, message, null);
        byte[] bytes = gson.toJson(body).getBytes("UTF-8");

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(status, bytes.length);
    }
}
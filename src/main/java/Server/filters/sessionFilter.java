package Server.filters;

import Server.service.UserService;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class sessionFilter extends Filter {

    private final UserService userService;

    public sessionFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {

        String authHeader = exchange.getRequestHeaders().getFirst("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sendUnauthorized(exchange);
            return;
        }

        String token = authHeader.substring(7);

        var user = userService.authenticate(token);

        if (user == null) {
            sendUnauthorized(exchange);
            return;
        }

        exchange.setAttribute("user", user);

        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "Auth Filter";
    }

    private void sendUnauthorized(HttpExchange exchange) throws IOException {
        String response = "Unauthorized";
        exchange.sendResponseHeaders(401, response.length());
        exchange.getResponseBody().write(response.getBytes());
        exchange.close();
    }
}
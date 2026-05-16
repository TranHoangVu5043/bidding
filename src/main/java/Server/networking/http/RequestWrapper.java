package Server.networking.http;

import Server.model.users.User;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public class RequestWrapper {

    private final HttpExchange exchange;

    public RequestWrapper(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public String getMethod() {
        return exchange.getRequestMethod();
    }

    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    public String getBody() throws IOException {
        return new String(exchange.getRequestBody().readAllBytes());
    }

    public String getHeader(String name) {
        return exchange.getRequestHeaders().getFirst(name);
    }

    public User getUser() {
        return (User) exchange.getAttribute("user");
    }

    public HttpExchange getExchange() {
        return exchange;
    }
}
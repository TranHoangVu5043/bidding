package Server.networking.http;

import Server.model.users.User;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestWrapper {

    private final HttpExchange exchange;
    private Map<String, String> pathParams = new HashMap<>();  // FIX: hỗ trợ path param {id}

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

    // ── Path params (set bởi ApiRouter khi match pattern route) ──

    public void setPathParams(Map<String, String> params) {
        this.pathParams = params != null ? params : new HashMap<>();
    }

    /** Lấy path param theo tên, ví dụ getPathParam("id") với route /api/users/{id}/ban */
    public String getPathParam(String name) {
        return pathParams.get(name);
    }

    public Map<String, String> getPathParams() {
        return Collections.unmodifiableMap(pathParams);
    }
}
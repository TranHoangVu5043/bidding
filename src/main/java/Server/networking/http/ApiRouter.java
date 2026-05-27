package Server.networking.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ApiRouter implements HttpHandler {

    private final Map<String, Handler> routes = new HashMap<>();

    public void register(String method,
                         String path,
                         Handler handler) {

        routes.put(method + ":" + path, handler);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        RequestWrapper req = new RequestWrapper(exchange);
        ResponseWrapper res = new ResponseWrapper(exchange);
        String method = req.getMethod().toUpperCase();
        String path   = req.getPath();
        System.out.println(method + ":" + path);

        String key = method + ":" + path;
        Handler handler = routes.get(key);

        if (handler == null) {
            res.send(404, "Route not found");
            return;
        }

        long start = System.currentTimeMillis();
        handler.handle(req, res);
        long ms = System.currentTimeMillis() - start;
        System.out.printf("[BENCH] SERVER  %-6s %-40s →  %d ms%n", method, path, ms);
    }
}
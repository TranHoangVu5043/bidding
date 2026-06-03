package Server.networking.http;

import Server.controller.responseObjects.ApiResponse;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class ResponseWrapper {

    private static final Logger log = LoggerFactory.getLogger(ResponseWrapper.class);

    private final HttpExchange exchange;
    private final Gson gson;

    public ResponseWrapper(HttpExchange exchange) {
        this.exchange = exchange;
        this.gson = new Gson();
    }

    public void send(int status, String body) {

        try {

            byte[] bytes = body.getBytes();

            exchange.sendResponseHeaders(status, bytes.length);

            OutputStream os = exchange.getResponseBody();

            os.write(bytes);
            os.close();

        } catch (Exception e) {

            log.error("Response send failed", e);
        }
    }

    public void sendJson(int status, String json) {

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        send(status, json);
    }

    public void error(int status, String message) {

        ApiResponse<Object> response = new ApiResponse<>(status, message, null);

        sendJson(status, gson.toJson(response));
    }
}
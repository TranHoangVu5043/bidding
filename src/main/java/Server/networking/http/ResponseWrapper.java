package Server.networking.http;

import Server.controller.responseObjects.ApiResponse;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.OutputStream;

public class ResponseWrapper {

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

            System.err.println("[ERROR] Response send failed: " + e.getMessage());

            e.printStackTrace();
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
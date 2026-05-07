package Server.networking.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

public interface Handler {
    void handle(RequestWrapper req, ResponseWrapper res) throws IOException;

    void handle(HttpExchange exchange) throws IOException;
}
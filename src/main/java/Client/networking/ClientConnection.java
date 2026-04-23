package Client.networking;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;

public class ClientConnection {

    private static ClientConnection instance;

    private final HttpClient client;
    private String baseUrl;
    private String token;

    private ClientConnection() {
        client = HttpClient.newHttpClient();
        baseUrl = "http://localhost:8080/api";
    }

    public static synchronized ClientConnection getInstance() {
        if (instance == null) {
            instance = new ClientConnection();
        }
        return instance;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String get(String path) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET();

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.build();

        HttpResponse<String> response =
                client.send(request, BodyHandlers.ofString());

        return formatResponse(response);
    }

    public String post(String path, String json) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json));

        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }

        HttpRequest request = builder.build();

        HttpResponse<String> response =
                client.send(request, BodyHandlers.ofString());

        return formatResponse(response);
    }

    private String formatResponse(HttpResponse<String> res) {
        return "Status: " + res.statusCode() + "\nBody: " + res.body();
    }
}
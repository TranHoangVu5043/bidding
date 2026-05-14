package Client.networking;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL =
            "http://localhost:8080/api";

    private final HttpClient client;
    private final Gson gson;

    public ApiClient() {

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.gson = new Gson();
    }

    // ===== GET =====

    public String get(String path)
            throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + path))
                        .GET();

        attachToken(builder);

        HttpRequest request = builder.build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        return response.body();
    }

    // ===== POST =====

    public String post(String path, Object body)
            throws IOException, InterruptedException {

        String json = gson.toJson(body);

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + path))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json));

        attachToken(builder);

        HttpRequest request = builder.build();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        return response.body();
    }

    // ===== TOKEN =====

    private void attachToken(HttpRequest.Builder builder) {

        String token = SessionManager.getToken();

        if (token != null) {
            builder.header("Authorization","Bearer " +  token);
        }
    }
}
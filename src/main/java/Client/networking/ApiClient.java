package Client.networking;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = "http://127.0.0.1:8080/api";

    // Shared across all ApiClient instances — reuses the connection pool
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final Gson gson;

    public ApiClient() {
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

        long start = System.currentTimeMillis();
        HttpResponse<String> response =
                client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        long ms = System.currentTimeMillis() - start;
        System.out.printf("[BENCH] CLIENT  GET    %-40s →  %d ms%n", path, ms);

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

        long start = System.currentTimeMillis();
        HttpResponse<String> response =
                client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        long ms = System.currentTimeMillis() - start;
        System.out.printf("[BENCH] CLIENT  POST   %-40s →  %d ms%n", path, ms);

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
package Client.networking;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {

    private static final String BASE_URL = ServerConfig.HTTP_BASE + "/api";

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

        HttpResponse<String> response =
                client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return guardBody(response);
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

        HttpResponse<String> response =
                client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return guardBody(response);
    }

    /**
     * Returns the response body unchanged when it looks like our server's JSON.
     * Our server always returns {"status": <integer>, "message": "...", "data": ...}.
     *
     * Railway (and other proxies) return their own error envelopes with a string
     * status field like {"status":"error","code":502,"message":"Application failed
     * to respond"}. Gson crashes trying to parse "error" as int. We intercept those
     * here and build a well-typed fallback so every caller sees a clean error.
     */
    private String guardBody(HttpResponse<String> response) {
        String body       = response.body();
        int    httpStatus = response.statusCode();

        // Our server's JSON always starts with '{' and has an integer status
        if (body != null && body.trim().startsWith("{")) {
            if (!body.contains("\"status\":\"error\"") &&
                !body.contains("\"status\": \"error\"")) {
                return body; // normal path — let Gson parse it
            }
        }

        // Railway / proxy error — try to extract a human-readable message
        String message = extractProxyMessage(body, httpStatus);
        // Escape any double quotes so the JSON stays valid
        message = message.replace("\"", "'");
        return "{\"status\":" + httpStatus
                + ",\"message\":\"" + message + "\""
                + ",\"data\":null}";
    }

    /**
     * Pulls the "message" field out of a Railway-style proxy error body, or
     * falls back to a generic Vietnamese message that makes sense to the user.
     */
    private String extractProxyMessage(String body, int httpStatus) {
        if (httpStatus == 502 || httpStatus == 503) {
            return "Server đang khởi động lại, vui lòng thử lại sau vài giây.";
        }
        if (httpStatus == 504) {
            return "Server không phản hồi (timeout). Vui lòng thử lại.";
        }
        if (body == null || body.isBlank()) {
            return "Không nhận được phản hồi từ Server (HTTP " + httpStatus + ").";
        }
        // Try to grab the "message" value from the proxy JSON without a full parse
        int idx = body.indexOf("\"message\":");
        if (idx >= 0) {
            int start = body.indexOf('"', idx + 10);
            if (start >= 0) {
                int end = body.indexOf('"', start + 1);
                if (end > start) {
                    return body.substring(start + 1, end);
                }
            }
        }
        return "Không thể kết nối đến Server (HTTP " + httpStatus + ").";
    }

    // ===== TOKEN =====

    private void attachToken(HttpRequest.Builder builder) {

        String token = SessionManager.getToken();

        if (token != null) {
            builder.header("Authorization","Bearer " +  token);
        }
    }
}
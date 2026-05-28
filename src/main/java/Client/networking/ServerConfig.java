package Client.networking;

/**
 * Central place for server URLs.
 *
 * Override at launch with JVM system properties:
 *   -Dserver.http=https://your-app.up.railway.app
 *   -Dserver.ws=ws://roundhouse.proxy.rlwy.net:12345
 *
 * Falls back to localhost for local development.
 */
public final class ServerConfig {

    public static final String HTTP_BASE =
            System.getProperty("server.http", "http://127.0.0.1:8080");

    public static final String WS_URL =
            System.getProperty("server.ws", "ws://localhost:8081");

    private ServerConfig() {}
}

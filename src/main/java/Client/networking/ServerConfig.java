package Client.networking;

// swap in your real Railway URLs here before packaging the client JAR
// HTTP_BASE  → your Railway service URL  (e.g. https://yourapp.up.railway.app)
// WS_URL     → your Railway TCP proxy URL (e.g. ws://roundhouse.proxy.rlwy.net:12345)
// both still fall back to localhost so local dev keeps working
public final class ServerConfig {

    public static final String HTTP_BASE =
            System.getProperty("server.http", "https://bidding-production-3e9a.up.railway.app");

    public static final String WS_URL =
            System.getProperty("server.ws", "ws://roundhouse.proxy.rlwy.net:43153");

    private ServerConfig() {}
}

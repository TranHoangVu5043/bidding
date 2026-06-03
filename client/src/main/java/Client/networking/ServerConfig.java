package Client.networking;

public final class ServerConfig {

    public static final String RAILWAY_HTTP = "https://bidding-production-3e9a.up.railway.app";
    public static final String RAILWAY_WS   = "ws://roundhouse.proxy.rlwy.net:43153";

    public static final String LOCAL_HTTP = "http://localhost:8080";
    public static final String LOCAL_WS = "ws://localhost:8081";


    public static final String HTTP_BASE =
            System.getProperty("server.http", RAILWAY_HTTP);

    public static final String WS_URL =
            System.getProperty("server.ws", RAILWAY_WS);

    private ServerConfig() {}
}

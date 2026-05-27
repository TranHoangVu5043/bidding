package Client.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.function.BiConsumer;

/**
 * Single persistent WebSocket connection for the main auction floor.
 * Supports subscribing to multiple auction rooms on one connection.
 *
 * Usage:
 *   client = new AuctionWebSocketClient((auctionId, newPrice) -> updateLabel(auctionId, newPrice));
 *   client.connect();                    // non-blocking, safe to call on FX thread
 *   client.subscribe(5);                 // subscribe to auction #5
 *   client.subscribe(7);                 // subscribe to auction #7
 *   ...
 *   client.closeConnection();            // on sign-out or scene change
 */
public class AuctionWebSocketClient extends WebSocketClient {

    private static final String WS_URL = "ws://localhost:8081";

    /**
     * Called on the JavaFX thread whenever a bid_update arrives.
     * Args: (auctionId, newCurrentPrice)
     */
    private final BiConsumer<Integer, Double> onBidUpdate;

    public AuctionWebSocketClient(BiConsumer<Integer, Double> onBidUpdate) {
        super(URI.create(WS_URL));
        this.onBidUpdate = onBidUpdate;
    }

    // ── WebSocketClient callbacks ────────────────────────────────────────

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[WS Client] Connected to " + WS_URL);
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (!"bid_update".equals(json.get("type").getAsString())) return;

            int    auctionId = json.get("auctionId").getAsInt();
            double newPrice  = json.get("currentPrice").getAsDouble();

            Platform.runLater(() -> onBidUpdate.accept(auctionId, newPrice));
        } catch (Exception e) {
            System.err.println("[WS Client] Parse error: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WS Client] Disconnected (code=" + code + ", reason=" + reason + ")");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WS Client] Error: " + ex.getMessage());
    }

    // ── Public API ───────────────────────────────────────────────────────

    /** Subscribe to live updates for one auction room. */
    public void subscribe(int auctionId) {
        if (!isOpen()) return;
        JsonObject msg = new JsonObject();
        msg.addProperty("type",      "subscribe");
        msg.addProperty("auctionId", auctionId);
        send(msg.toString());
    }

    /** Unsubscribe from one auction room. */
    public void unsubscribe(int auctionId) {
        if (!isOpen()) return;
        JsonObject msg = new JsonObject();
        msg.addProperty("type",      "unsubscribe");
        msg.addProperty("auctionId", auctionId);
        send(msg.toString());
    }

    /** Gracefully close the connection. Safe to call even if already closed. */
    public void closeConnection() {
        if (isOpen()) close();
    }
}

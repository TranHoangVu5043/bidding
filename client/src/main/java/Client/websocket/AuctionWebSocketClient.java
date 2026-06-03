package Client.websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class AuctionWebSocketClient extends WebSocketClient {

    private static final String WS_URL = Client.networking.ServerConfig.WS_URL;

    private final Consumer<BidUpdate> onBidUpdate;
    /** Auction IDs to (re-)subscribe whenever the connection opens. */
    private final Set<Integer> subscribedRooms = ConcurrentHashMap.newKeySet();
    /** Set to false on explicit close so we don't loop-reconnect after sign-out. */
    private volatile boolean autoReconnect = true;

    public AuctionWebSocketClient(Consumer<BidUpdate> onBidUpdate) {
        super(URI.create(WS_URL));
        this.onBidUpdate = onBidUpdate;
    }

    // ── WebSocketClient callbacks ──────────────────────────────────────────────

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[WS] Connected → re-subscribing to " + subscribedRooms);
        subscribedRooms.forEach(this::sendSubscribe);
    }

    @Override
    public void onMessage(String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (!"bid_update".equals(json.get("type").getAsString())) return;

            int    auctionId  = json.get("auctionId").getAsInt();
            double newPrice   = json.get("currentPrice").getAsDouble();
            String newEndTime = json.has("newEndTime") && !json.get("newEndTime").isJsonNull()
                    ? json.get("newEndTime").getAsString() : null;

            Platform.runLater(() -> onBidUpdate.accept(new BidUpdate(auctionId, newPrice, newEndTime)));
        } catch (Exception e) {
            System.err.println("[WS] Parse error: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WS] Disconnected (code=" + code + ") — autoReconnect=" + autoReconnect);
        if (!autoReconnect) return;
        new Thread(() -> {
            try { Thread.sleep(3_000); } catch (InterruptedException ignored) {}
            try { reconnect(); } catch (Exception e) {
                System.err.println("[WS] Reconnect failed: " + e.getMessage());
            }
        }, "ws-reconnect").start();
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WS] Error: " + ex.getMessage());
    }

    // ── Public API ─────────────────────────────────────────────────────────────

    /** Subscribe to live updates for an auction room. Safe to call before connect(). */
    public void subscribe(int auctionId) {
        subscribedRooms.add(auctionId);
        if (isOpen()) sendSubscribe(auctionId);
    }

    /** Unsubscribe from an auction room. */
    public void unsubscribe(int auctionId) {
        subscribedRooms.remove(auctionId);
        if (!isOpen()) return;
        JsonObject msg = new JsonObject();
        msg.addProperty("type",      "unsubscribe");
        msg.addProperty("auctionId", auctionId);
        send(msg.toString());
    }

    /** Explicitly close — disables auto-reconnect (sign-out / scene change). */
    public void closeConnection() {
        autoReconnect = false;
        if (!isClosed()) close();
    }

    // ── private ────────────────────────────────────────────────────────────────

    private void sendSubscribe(int auctionId) {
        JsonObject msg = new JsonObject();
        msg.addProperty("type",      "subscribe");
        msg.addProperty("auctionId", auctionId);
        send(msg.toString());
    }
}

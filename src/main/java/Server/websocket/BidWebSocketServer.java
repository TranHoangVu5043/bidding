package Server.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class BidWebSocketServer extends WebSocketServer {

    private static final Logger log = LoggerFactory.getLogger(BidWebSocketServer.class);
    private static volatile BidWebSocketServer instance;
    private static final int WS_PORT =
            Integer.parseInt(System.getenv().getOrDefault("WS_PORT", "8081"));

    /** auctionId → set of subscribed connections */
    private final Map<Integer, Set<WebSocket>> rooms = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private BidWebSocketServer(int port) {
        super(new InetSocketAddress(port));
        setReuseAddr(true);
        setConnectionLostTimeout(60);
    }

    public static BidWebSocketServer getInstance() {
        if (instance == null) {
            synchronized (BidWebSocketServer.class) {
                if (instance == null) {
                    instance = new BidWebSocketServer(WS_PORT);
                }
            }
        }
        return instance;
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        log.debug("Client connected: {}", conn.getRemoteSocketAddress());
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        rooms.values().forEach(set -> set.remove(conn));
        log.debug("Client disconnected: {}", conn.getRemoteSocketAddress());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            String type = json.get("type").getAsString();
            int auctionId = json.get("auctionId").getAsInt();

            if ("subscribe".equals(type)) {
                rooms.computeIfAbsent(auctionId,
                        k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(conn);
                log.debug("Subscribed to auction #{} (room size: {})",
                        auctionId, rooms.get(auctionId).size());
            } else if ("unsubscribe".equals(type)) {
                Set<WebSocket> room = rooms.get(auctionId);
                if (room != null) room.remove(conn);
            }
        } catch (Exception e) {
            log.warn("Bad message from {}: {}", conn.getRemoteSocketAddress(), e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        log.error("WebSocket error on {}",
                conn != null ? conn.getRemoteSocketAddress() : "server", ex);
    }

    @Override
    public void onStart() {
        log.info("WebSocket server listening on port {}", WS_PORT);
    }

    /**
     * Broadcast a bid update to every client subscribed to this auction.
     * Called by BiddingService after a successful bid commit.
     */
    /**
     * @param newEndTime ISO-8601 string of the reset end-time when anti-snipe fired, or {@code null}.
     */
    public void broadcastBidUpdate(int auctionId, double currentPrice, int userId, double amount, String newEndTime) {
        Set<WebSocket> room = rooms.get(auctionId);
        if (room == null || room.isEmpty()) return;

        JsonObject payload = new JsonObject();
        payload.addProperty("type",         "bid_update");
        payload.addProperty("auctionId",    auctionId);
        payload.addProperty("currentPrice", currentPrice);
        payload.addProperty("userId",       userId);
        payload.addProperty("amount",       amount);
        if (newEndTime != null) payload.addProperty("newEndTime", newEndTime);
        String json = gson.toJson(payload);

        room.stream()
            .filter(WebSocket::isOpen)
            .forEach(c -> c.send(json));

        log.info("Broadcast bid_update → auction #{} price={} to {} client(s)",
                auctionId, currentPrice, room.size());
    }
}

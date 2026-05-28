package Client.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * WebSocket client kết nối tới BidWebSocketServer.
 * Nhận real-time bid updates và callback lên UI thread.
 */
public class AuctionWebSocketClient extends WebSocketClient {

    private static final String SERVER_URI = "ws://localhost:8081";

    /**
     * Callback interface — gọi khi nhận BID_UPDATE từ server.
     * Chạy trên JavaFX Application Thread (Platform.runLater đã được xử lý bên trong).
     */
    public interface BidUpdateCallback {
        void onBidUpdate(int auctionId, double newPrice);
    }

    private final BidUpdateCallback callback;

    public AuctionWebSocketClient(BidUpdateCallback callback) {
        super(toUri(SERVER_URI));
        this.callback = callback;
    }

    // ══════════════════════════════════════════
    //  WebSocketClient callbacks
    // ══════════════════════════════════════════

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("[WS Client] Kết nối tới server thành công: " + SERVER_URI);
    }

    @Override
    public void onMessage(String message) {
        System.out.println("[WS Client] Nhận: " + message);
        try {
            // Parse JSON thủ công — tránh phụ thuộc thêm thư viện
            // Expected format: {"type":"BID_UPDATE","auctionId":1,"newPrice":500000,"bidder":"...","amount":500000}
            if (message.contains("\"BID_UPDATE\"")) {
                int auctionId = parseIntField(message, "auctionId");
                double newPrice = parseDoubleField(message, "newPrice");
                if (auctionId >= 0 && callback != null) {
                    javafx.application.Platform.runLater(() -> callback.onBidUpdate(auctionId, newPrice));
                }
            } else if (message.contains("\"AUCTION_ENDED\"")) {
                System.out.println("[WS Client] Phiên đấu giá đã kết thúc: " + message);
                // Có thể mở rộng thêm callback cho AUCTION_ENDED nếu cần
            }
        } catch (Exception e) {
            System.err.println("[WS Client] Lỗi parse message: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("[WS Client] Ngắt kết nối — code: " + code + ", lý do: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("[WS Client] Lỗi WebSocket: " + ex.getMessage());
    }

    // ══════════════════════════════════════════
    //  Public API
    // ══════════════════════════════════════════

    /**
     * Đăng ký nhận update cho một phiên đấu giá cụ thể.
     * Gửi message: {"type":"SUBSCRIBE","auctionId":123}
     */
    public void subscribe(int auctionId) {
        if (isOpen()) {
            send("{\"type\":\"SUBSCRIBE\",\"auctionId\":" + auctionId + "}");
            System.out.println("[WS Client] Subscribed to auction #" + auctionId);
        } else {
            System.err.println("[WS Client] Chưa kết nối — không thể subscribe auction #" + auctionId);
        }
    }

    /**
     * Đóng kết nối WebSocket an toàn.
     */
    public void closeConnection() {
        if (!isClosed()) {
            close();
            System.out.println("[WS Client] Đã đóng kết nối.");
        }
    }

    // ══════════════════════════════════════════
    //  Helpers
    // ══════════════════════════════════════════

    private static URI toUri(String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("URI WebSocket không hợp lệ: " + uri, e);
        }
    }

    /** Trích giá trị int từ JSON string đơn giản. */
    private int parseIntField(String json, String field) {
        try {
            String key = "\"" + field + "\":";
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    /** Trích giá trị double từ JSON string đơn giản. */
    private double parseDoubleField(String json, String field) {
        try {
            String key = "\"" + field + "\":";
            int start = json.indexOf(key) + key.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
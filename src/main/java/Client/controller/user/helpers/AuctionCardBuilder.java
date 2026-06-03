package Client.controller.user.helpers;

import Client.controller.user.dialogs.AuctionDetailDialog;
import Client.controller.user.dialogs.AutoBidDialog;
import Client.controller.user.dialogs.BidHistoryDialog;
import Client.controller.user.dialogs.PlaceBidDialog;
import Client.model.auction.Auction;
import Client.networking.endpoints.BidApi;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class AuctionCardBuilder {


    public record Config(
            Supplier<Window>          windowSupplier,
            BidApi                    bidApi,
            Map<Integer, Label>       livePriceLabels,
            Map<Integer, Label>       liveStatusLabels,
            Map<Integer, Label>       liveTimeLabels,
            Map<Integer, Button>      liveBidButtons,
            Map<Integer, Button>      liveAutoBidBtns,
            Consumer<Double>          onBidSuccess,
            Function<String, String>  formatTimeRemaining
    ) {}

    private final Config cfg;

    public AuctionCardBuilder(Config config) {
        this.cfg = config;
    }

    public VBox build(Auction auction) {
        String status = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";

        // ── Status badge ──
        String badgeColor = switch (status) {
            case "ACTIVE"   -> "#16A34A";
            case "UPCOMING" -> "#0066CC";
            case "FINISHED" -> "#6B7280";
            default         -> "#D32F2F";
        };
        String statusText = switch (status) {
            case "ACTIVE"   -> "● Đang diễn ra";
            case "UPCOMING" -> "● Sắp diễn ra";
            case "FINISHED" -> "✓ Đã kết thúc";
            default         -> "✕ Đã hủy";
        };
        Label lblStatus = new Label(statusText);
        lblStatus.setStyle("-fx-text-fill: " + badgeColor + "; -fx-font-size: 11; -fx-font-weight: bold;");

        // ── Image area (click → detail popup) ──
        Region imgBg = new Region();
        imgBg.setPrefSize(186, 120);
        imgBg.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 8;");

        String itemDisplay = (auction.getItemName() != null && !auction.getItemName().isBlank())
                ? auction.getItemName() : "Mặt hàng #" + auction.getItemId();
        Label imgLabel = new Label("🏷  " + itemDisplay);
        imgLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        Label hintLabel = new Label("Nhấn để xem chi tiết");
        hintLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF; -fx-padding: 4 0 0 0;");

        VBox imgContent = new VBox(4, imgLabel, hintLabel);
        imgContent.setAlignment(Pos.CENTER);
        StackPane imgPane = new StackPane(imgBg, imgContent);
        imgPane.setStyle("-fx-cursor: hand;");
        imgPane.setOnMouseClicked(e -> AuctionDetailDialog.show(
                cfg.windowSupplier().get(), auction,
                () -> PlaceBidDialog.show(auction, cfg.bidApi(), cfg.onBidSuccess()),
                () -> AutoBidDialog.show(auction, cfg.bidApi()),
                () -> BidHistoryDialog.show(auction, cfg.bidApi())));

        // ── Auction info labels ──
        Label lblTitle = new Label("Phiên đấu giá #" + auction.getId());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        lblTitle.setWrapText(true);

        Label lblSeller = new Label("🏪 " + (auction.getSellerName() != null ? auction.getSellerName() : "—"));
        lblSeller.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        Label lblStartPrice = new Label("Khởi điểm: " + String.format("%,.0f ₫", auction.getStartingPrice()));
        lblStartPrice.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");

        Label lblCurrentPrice = new Label("Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        lblCurrentPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #D32F2F;");

        // Register for live WebSocket price/status updates
        if ("ACTIVE".equals(status)) {
            cfg.livePriceLabels().put(auction.getId(), lblCurrentPrice);
            cfg.liveStatusLabels().put(auction.getId(), lblStatus);
        }

        // ── Countdown label ──
        Label lblTime = new Label("🕒 " + cfg.formatTimeRemaining().apply(auction.getEndTime()));
        lblTime.setStyle("-fx-font-size: 11; -fx-text-fill: #0066CC;");
        lblTime.setUserData(auction.getEndTime());   // countdown ticker reads this
        if ("ACTIVE".equals(status)) cfg.liveTimeLabels().put(auction.getId(), lblTime);

        // ── Bid buttons ──
        boolean canBid = "ACTIVE".equals(status);

        Button btnBid = new Button("Đặt giá ngay");
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setDisable(!canBid);
        btnBid.setStyle("-fx-background-color: " + (canBid ? "#0066CC" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8;");
        if (canBid) {
            btnBid.setOnAction(e -> PlaceBidDialog.show(auction, cfg.bidApi(), cfg.onBidSuccess()));
            cfg.liveBidButtons().put(auction.getId(), btnBid);
        }

        Button btnAutoBid = new Button("🤖 Đặt giá tự động");
        btnAutoBid.setMaxWidth(Double.MAX_VALUE);
        btnAutoBid.setDisable(!canBid);
        btnAutoBid.setStyle("-fx-background-color: " + (canBid ? "#7C3AED" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: " + (canBid ? "hand" : "default") + "; -fx-padding: 7;");
        if (canBid) {
            btnAutoBid.setOnAction(e -> AutoBidDialog.show(auction, cfg.bidApi()));
            cfg.liveAutoBidBtns().put(auction.getId(), btnAutoBid);
        }

        Button btnHistory = new Button("Lịch sử đấu giá");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6;");
        btnHistory.setOnAction(e -> BidHistoryDialog.show(auction, cfg.bidApi()));

        // ── Assemble ──
        VBox card = new VBox(8, lblStatus, imgPane, lblTitle, lblSeller,
                lblStartPrice, lblCurrentPrice, lblTime,
                btnBid, btnAutoBid, btnHistory);
        card.setPrefWidth(210);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 10, 0, 0, 3); " +
                "-fx-padding: 12;");
        return card;
    }
}

package Client.controller.seller.helpers;

import Client.controller.seller.dialogs.SellerAuctionDetailDialog;
import Client.controller.user.dialogs.BidHistoryDialog;
import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.BidApi;
import Client.util.DialogUtil;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class SellerCardBuilder {

    public record Config(
            Supplier<Window>        windowSupplier,
            AuctionApi              auctionApi,
            BidApi                  bidApi,
            Map<Integer, Label>     livePriceLabels,
            Map<Integer, Label>     liveStatusLabels,
            Map<Integer, Label>     liveTimeLabels,
            Map<Integer, Label>     liveHighestBidderLabels,
            Map<Integer, Button>    liveFinishBtns,
            Map<Integer, Button>    liveCancelBtns,
            Runnable                onActionComplete,
            Function<String, String> formatTimeRemaining
    ) {}

    private final Config cfg;

    public SellerCardBuilder(Config config) {
        this.cfg = config;
    }

    public VBox build(Auction auction) {
        String status = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";
        String category = auction.getItemCategory() != null ? auction.getItemCategory() : ""; // used only by detail dialog

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

        // Image area — click opens detail dialog
        Region imgBg = new Region();
        imgBg.setPrefSize(186, 120);
        imgBg.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 8;");

        String itemDisplay = (auction.getItemName() != null && !auction.getItemName().isBlank())
                ? auction.getItemName() : "SP #" + auction.getItemId();
        Label imgLabel = new Label("🏷  " + itemDisplay);
        imgLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        Label hintLabel = new Label("Nhấn để xem chi tiết");
        hintLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF; -fx-padding: 4 0 0 0;");

        VBox imgContent = new VBox(4, imgLabel, hintLabel);
        imgContent.setAlignment(Pos.CENTER);
        StackPane imgPane = new StackPane(imgBg, imgContent);
        imgPane.setStyle("-fx-cursor: hand;");
        imgPane.setOnMouseClicked(e -> SellerAuctionDetailDialog.show(
                cfg.windowSupplier().get(), auction, itemDisplay, category,
                cfg.auctionApi(), cfg.onActionComplete()));

        // Info labels
        Label lblSeller = new Label("🏪 " + (auction.getSellerName() != null ? auction.getSellerName() : "—"));
        lblSeller.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        Label lblStartPrice = new Label("Khởi điểm: " + String.format("%,.0f ₫", auction.getStartingPrice()));
        lblStartPrice.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");

        Label lblCurrentPrice = new Label("Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        lblCurrentPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #D32F2F;");

        String bidderText = auction.getHighestBidderName() != null
                ? "🏆 " + auction.getHighestBidderName() : "🏆 Chưa có";
        Label lblHighestBidder = new Label(bidderText);
        lblHighestBidder.setStyle("-fx-font-size: 11; -fx-text-fill: #7C3AED;");

        Label lblTime = new Label("🕒 " + cfg.formatTimeRemaining().apply(auction.getEndTime()));
        lblTime.setStyle("-fx-font-size: 11; -fx-text-fill: #0066CC;");
        lblTime.setUserData(auction.getEndTime());

        if ("ACTIVE".equals(status)) {
            cfg.livePriceLabels().put(auction.getId(), lblCurrentPrice);
            cfg.liveStatusLabels().put(auction.getId(), lblStatus);
            cfg.liveTimeLabels().put(auction.getId(), lblTime);
            cfg.liveHighestBidderLabels().put(auction.getId(), lblHighestBidder);
        }

        // Buttons
        Button btnFinish = new Button("⏹ Kết thúc sớm");
        btnFinish.setMaxWidth(Double.MAX_VALUE);
        btnFinish.setDisable(!"ACTIVE".equals(status));
        btnFinish.setStyle("-fx-background-color: " + ("ACTIVE".equals(status) ? "#f97316" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: hand;");
        if ("ACTIVE".equals(status)) {
            btnFinish.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận kết thúc sớm");
                confirm.setHeaderText(null);
                confirm.setContentText("Kết thúc phiên #" + auction.getId() + " ngay bây giờ?\nNgười đang dẫn đầu sẽ thắng.");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp != ButtonType.OK) return;
                    new Thread(() -> {
                        ApiResponse<Void> res = cfg.auctionApi().finishAuction(auction.getId());
                        Platform.runLater(() -> {
                            if (res != null && res.getStatus() == 200) {
                                if (cfg.onActionComplete() != null) cfg.onActionComplete().run();
                            } else {
                                SceneUtil.showAlert("Lỗi", res != null ? res.getMessage() : "Mất kết nối");
                            }
                        });
                    }).start();
                });
            });
            cfg.liveFinishBtns().put(auction.getId(), btnFinish);
        }

        boolean canCancel = "ACTIVE".equals(status) || "UPCOMING".equals(status);
        Button btnCancel = new Button("✕ Hủy phiên");
        btnCancel.setMaxWidth(Double.MAX_VALUE);
        btnCancel.setDisable(!canCancel);
        btnCancel.setStyle("-fx-background-color: " + (canCancel ? "#fee2e2" : "#F3F4F6") + "; " +
                "-fx-text-fill: " + (canCancel ? "#b91c1c" : "#9CA3AF") + "; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8; -fx-cursor: " + (canCancel ? "hand" : "default") + ";");
        if (canCancel) {
            btnCancel.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận hủy");
                confirm.setHeaderText(null);
                confirm.setContentText("Bạn có chắc muốn hủy phiên #" + auction.getId() + "?");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp != ButtonType.OK) return;
                    new Thread(() -> {
                        ApiResponse<Void> res = cfg.auctionApi().cancelAuction(auction.getId());
                        Platform.runLater(() -> {
                            if (res != null && res.getStatus() == 200) {
                                if (cfg.onActionComplete() != null) cfg.onActionComplete().run();
                            } else {
                                SceneUtil.showAlert("Lỗi hủy", res != null ? res.getMessage() : "Mất kết nối");
                            }
                        });
                    }).start();
                });
            });
            cfg.liveCancelBtns().put(auction.getId(), btnCancel);
        }

        Button btnDetail = new Button("🔍 Xem chi tiết");
        btnDetail.setMaxWidth(Double.MAX_VALUE);
        btnDetail.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #0066CC; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8;");
        btnDetail.setOnAction(e -> SellerAuctionDetailDialog.show(
                cfg.windowSupplier().get(), auction, itemDisplay, category,
                cfg.auctionApi(), cfg.onActionComplete()));

        Button btnHistory = new Button("📋 Lịch sử đấu giá");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6;");
        btnHistory.setOnAction(e -> BidHistoryDialog.show(auction, cfg.bidApi()));

        VBox card = new VBox(8, lblStatus, imgPane, lblSeller,
                lblStartPrice, lblCurrentPrice, lblHighestBidder, lblTime,
                btnFinish, btnCancel, btnDetail, btnHistory);
        card.setPrefWidth(210);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 10, 0, 0, 3); -fx-padding: 12;");
        return card;
    }

    // Keep item card as static — it doesn't need live updates
    public static VBox buildItemCard(Item item, List<Auction> sellerAuctions) {
        Label iconLabel = new Label(DialogUtil.categoryEmoji(item.getCategory()));
        iconLabel.setStyle("-fx-font-size: 38;");

        javafx.scene.layout.StackPane imageArea = new javafx.scene.layout.StackPane(iconLabel);
        imageArea.setPrefSize(160, 110);
        imageArea.setStyle("-fx-background-color: " + DialogUtil.categoryGradient(item.getCategory()) +
                "; -fx-background-radius: 10 10 0 0;");

        Label priceLabel = new Label(String.format("$ %,.0f", item.getPrice()));
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 4 8;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(144);
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String catColor = DialogUtil.categoryColor(item.getCategory());
        Label catLabel = new Label(item.getCategory() != null ? item.getCategory() : "OTHER");
        catLabel.setStyle("-fx-background-color: " + catColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");

        long activeCount = sellerAuctions.stream()
                .filter(a -> a.getItemId() == item.getId()
                        && ("ACTIVE".equalsIgnoreCase(a.getStatus())
                        ||  "UPCOMING".equalsIgnoreCase(a.getStatus())))
                .count();
        boolean isOnAuction = activeCount > 0;
        boolean isActive    = sellerAuctions.stream()
                .anyMatch(a -> a.getItemId() == item.getId()
                        && "ACTIVE".equalsIgnoreCase(a.getStatus()));

        Label stockLabel = new Label("Kho: " + item.getStock()
                + (isOnAuction ? "  •  " + activeCount + " đấu giá" : ""));
        stockLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #9ca3af;");

        HBox catBox = new HBox(6, catLabel);
        catBox.setPadding(new javafx.geometry.Insets(4, 0, 0, 0));
        VBox info = new VBox(3, nameLabel, catBox, stockLabel);
        info.setPadding(new javafx.geometry.Insets(7, 8, 10, 8));

        if (isOnAuction) {
            String overlayColor = isActive ? "#22c55e" : "#3b82f6";
            String overlayText  = isActive ? "● Đang diễn ra" : "● Sắp diễn ra";
            Label overlay = new Label(overlayText);
            overlay.setStyle("-fx-background-color: " + overlayColor + "; -fx-text-fill: white;" +
                    "-fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 0 0 6 0;");
            javafx.scene.layout.StackPane.setAlignment(overlay, javafx.geometry.Pos.TOP_LEFT);
            imageArea.getChildren().add(overlay);
        }

        VBox card = new VBox(imageArea, priceLabel, info);
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,3); -fx-cursor: hand;");
        return card;
    }
}

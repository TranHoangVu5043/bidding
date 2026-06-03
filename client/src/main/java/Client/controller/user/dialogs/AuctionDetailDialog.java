package Client.controller.user.dialogs;

import Client.model.auction.Auction;
import Client.util.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class AuctionDetailDialog {

    private AuctionDetailDialog() {}

    public static void show(
            Window owner,
            Auction auction,
            Runnable onBidRequested,
            Runnable onAutoBidRequested,
            Runnable onHistoryRequested) {

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) popup.initOwner(owner);
        popup.setTitle("Chi tiết phiên đấu giá #" + auction.getId());
        popup.setResizable(false);

        String status = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";

        String headerBg = switch (status) {
            case "ACTIVE"   -> "linear-gradient(to bottom, #dbeafe, #93c5fd)";
            case "UPCOMING" -> "linear-gradient(to bottom, #e0e7ff, #a5b4fc)";
            case "FINISHED" -> "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
            default         -> "linear-gradient(to bottom, #fee2e2, #fca5a5)";
        };
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

        //  Header 
        Label iconLabel = new Label("🏷");
        iconLabel.setStyle("-fx-font-size: 52;");

        Label titleLabel = new Label("Phiên đấu giá #" + auction.getId());
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label statusBadge = new Label(statusText);
        statusBadge.setStyle("-fx-text-fill: " + badgeColor +
                "; -fx-font-size: 12; -fx-font-weight: bold;");

        VBox header = new VBox(8, iconLabel, titleLabel, statusBadge);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 24, 16, 24));
        header.setStyle("-fx-background-color: " + headerBg + ";");

        //  Detail rows 
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 4, 28));

        String itemDisplay = (auction.getItemName() != null && !auction.getItemName().isBlank())
                ? auction.getItemName() : "#" + auction.getItemId();
        DialogUtil.addDetailRow(grid, 0, "📦 Sản phẩm",    itemDisplay);
        DialogUtil.addDetailRow(grid, 1, "💵 Giá khởi điểm", String.format("%,.0f ₫", auction.getStartingPrice()));
        DialogUtil.addDetailRow(grid, 2, "🔥 Giá hiện tại",  String.format("%,.0f ₫", auction.getCurrentPrice()));
        DialogUtil.addDetailRow(grid, 3, "🕐 Bắt đầu",       DialogUtil.formatDisplayTime(auction.getStartTime()));
        DialogUtil.addDetailRow(grid, 4, "🕔 Kết thúc",       DialogUtil.formatDisplayTime(auction.getEndTime()));

        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 24, 0, 24));

        //  Action buttons 
        boolean canBid = "ACTIVE".equals(status);

        Button btnBid = new Button("Đặt giá ngay  💸");
        btnBid.setDisable(!canBid);
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setStyle("-fx-background-color: " + (canBid ? "#0066CC" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: " + (canBid ? "hand" : "default") + ";");
        if (canBid) {
            btnBid.setOnAction(e -> { popup.close(); if (onBidRequested != null) onBidRequested.run(); });
        }

        Button btnAutoBid = new Button("🤖 Đặt giá tự động");
        btnAutoBid.setDisable(!canBid);
        btnAutoBid.setMaxWidth(Double.MAX_VALUE);
        btnAutoBid.setStyle("-fx-background-color: " + (canBid ? "#7C3AED" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: " + (canBid ? "hand" : "default") + ";");
        if (canBid) {
            btnAutoBid.setOnAction(e -> { popup.close(); if (onAutoBidRequested != null) onAutoBidRequested.run(); });
        }

        Button btnHistory = new Button("Lịch sử đặt giá  📋");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
        btnHistory.setOnAction(e -> { popup.close(); if (onHistoryRequested != null) onHistoryRequested.run(); });

        Button btnClose = new Button("← Quay lại");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());

        VBox btnBox = new VBox(8, btnBid, btnAutoBid, btnHistory, btnClose);
        btnBox.setPadding(new Insets(14, 28, 24, 28));

        VBox root = new VBox(header, grid, sep, btnBox);
        root.setStyle("-fx-background-color: #f8fafc;");

        popup.setScene(new Scene(root, 420, 520));
        popup.showAndWait();
    }
}

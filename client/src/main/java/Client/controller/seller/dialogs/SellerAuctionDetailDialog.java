package Client.controller.seller.dialogs;

import Client.model.auction.Auction;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.util.DialogUtil;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public final class SellerAuctionDetailDialog {

    private SellerAuctionDetailDialog() {}

    public static void show(
            Window owner,
            Auction auction,
            String itemName,
            String category,
            AuctionApi auctionApi,
            Runnable onCancelled) {

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);
        popup.setTitle("Chi tiết phiên đấu giá #" + auction.getId());
        popup.setResizable(false);

        String status    = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";
        String headerBg  = switch (status) {
            case "ACTIVE"   -> DialogUtil.categoryGradient(category);
            case "UPCOMING" -> "linear-gradient(to bottom, #e0e7ff, #a5b4fc)";
            case "FINISHED" -> "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
            default         -> "linear-gradient(to bottom, #fee2e2, #fca5a5)";
        };
        String badgeColor = DialogUtil.statusColor(status);
        String statusText = switch (status) {
            case "ACTIVE"   -> "● Đang diễn ra";
            case "UPCOMING" -> "● Sắp diễn ra";
            case "FINISHED" -> "✓ Đã kết thúc";
            default         -> "✕ Đã hủy";
        };

        //  Header 
        Label iconLabel = new Label(DialogUtil.categoryEmoji(category));
        iconLabel.setStyle("-fx-font-size: 52;");

        Label titleLabel = new Label(itemName);
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(360);

        Label subLabel = new Label("Phiên đấu giá #" + auction.getId());
        subLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #475569;");

        Label statusBadge = new Label(statusText);
        statusBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 20;");

        VBox header = new VBox(6, iconLabel, titleLabel, subLabel, statusBadge);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 24, 16, 24));
        header.setStyle("-fx-background-color: " + headerBg + ";");

        //  Detail rows 
        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 16, 28));
        DialogUtil.addDetailRow(grid, 0, "💵 Giá khởi điểm", String.format("%,.0f ₫", auction.getStartingPrice()));
        DialogUtil.addDetailRow(grid, 1, "🔥 Giá hiện tại",  String.format("%,.0f ₫", auction.getCurrentPrice()));
        DialogUtil.addDetailRow(grid, 2, "🕐 Bắt đầu",       DialogUtil.formatDisplayTime(auction.getStartTime()));
        DialogUtil.addDetailRow(grid, 3, "🕔 Kết thúc",       DialogUtil.formatDisplayTime(auction.getEndTime()));

        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 20, 0, 20));

        //  Action buttons 
        VBox btnBox = new VBox(8);
        btnBox.setPadding(new Insets(14, 28, 24, 28));

        if ("ACTIVE".equals(status)) {
            Button btnFinish = new Button("⏹  Kết thúc phiên sớm");
            btnFinish.setMaxWidth(Double.MAX_VALUE);
            btnFinish.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
            btnFinish.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận kết thúc sớm");
                confirm.setHeaderText(null);
                confirm.setContentText("Kết thúc phiên #" + auction.getId() + " ngay bây giờ?\n" +
                        "Người đang dẫn đầu sẽ được tính là người thắng.");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp != ButtonType.OK) return;
                    new Thread(() -> {
                        ApiResponse<Void> res = auctionApi.finishAuction(auction.getId());
                        Platform.runLater(() -> {
                            popup.close();
                            if (res != null && res.getStatus() == 200) {
                                SceneUtil.showAlert("Thành công", "Phiên đấu giá đã kết thúc sớm.");
                                if (onCancelled != null) onCancelled.run();
                            } else {
                                SceneUtil.showAlert("Lỗi", res != null ? res.getMessage() : "Mất kết nối");
                            }
                        });
                    }).start();
                });
            });
            btnBox.getChildren().add(btnFinish);
        }

        boolean canCancel = "ACTIVE".equals(status) || "UPCOMING".equals(status);
        if (canCancel) {
            Button btnCancel = new Button("✕  Hủy phiên đấu giá này");
            btnCancel.setMaxWidth(Double.MAX_VALUE);
            btnCancel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
            btnCancel.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận hủy");
                confirm.setHeaderText(null);
                confirm.setContentText("Bạn có chắc muốn hủy phiên đấu giá #" + auction.getId() + "?");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp != ButtonType.OK) return;
                    new Thread(() -> {
                        ApiResponse<Void> res = auctionApi.cancelAuction(auction.getId());
                        Platform.runLater(() -> {
                            popup.close();
                            if (res != null && res.getStatus() == 200) {
                                SceneUtil.showAlert("Thành công",
                                        "Đã hủy phiên đấu giá #" + auction.getId() + ".");
                                if (onCancelled != null) onCancelled.run();
                            } else {
                                SceneUtil.showAlert("Lỗi hủy",
                                        res != null ? res.getMessage() : "Mất kết nối");
                            }
                        });
                    }).start();
                });
            });
            btnBox.getChildren().add(btnCancel);
        }

        Button btnClose = new Button("← Quay lại");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());
        btnBox.getChildren().add(btnClose);

        VBox root = new VBox(header, grid, sep, btnBox);
        root.setStyle("-fx-background-color: #f8fafc;");

        int height = 440 + ("ACTIVE".equals(status) ? 55 : 0) + (canCancel ? 55 : 0);
        popup.setScene(new Scene(root, 420, height));
        popup.showAndWait();
    }
}

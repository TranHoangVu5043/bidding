package Client.controller.seller.dialogs;

import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import Client.util.DialogUtil;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;
import java.util.function.Consumer;

public final class ItemDetailDialog {

    private ItemDetailDialog() {}

    public static void show(
            Window owner,
            Item item,
            List<Auction> sellerAuctions,
            AuctionApi auctionApi,
            ItemApi itemApi,
            Runnable onAuctionCreated,
            Consumer<Item> onItemDeleted,
            Consumer<Item> onEditRequested) {

        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(owner);
        popup.setTitle("Chi tiết sản phẩm");
        popup.setResizable(false);

        // ── Header ──
        Label emoji = new Label(DialogUtil.categoryEmoji(item.getCategory()));
        emoji.setStyle("-fx-font-size: 52;");

        Label nameLabel = new Label(item.getName() != null ? item.getName() : "—");
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(360);

        Label catBadge = new Label(item.getCategory() != null ? item.getCategory() : "OTHER");
        catBadge.setStyle("-fx-background-color: " + DialogUtil.categoryColor(item.getCategory()) +
                "; -fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;" +
                " -fx-padding: 3 9; -fx-background-radius: 20;");

        VBox header = new VBox(8, emoji, nameLabel, catBadge);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 24, 16, 24));
        header.setStyle("-fx-background-color: " + DialogUtil.categoryGradient(item.getCategory()) + ";");

        // ── Detail rows ──
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 4, 28));
        DialogUtil.addDetailRow(grid, 0, "🔧 Tình trạng",  item.getCondition());
        DialogUtil.addDetailRow(grid, 1, "💰 Giá gốc",      String.format("%,.0f ₫", item.getPrice()));
        DialogUtil.addDetailRow(grid, 2, "📦 Tồn kho",      String.valueOf(item.getStock()));
        DialogUtil.addDetailRow(grid, 3, "📊 Trạng thái",   item.getStatus());

        // ── Description ──
        Label descTitle = new Label("📝 Mô tả");
        descTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13;");

        String descText = (item.getDescription() != null && !item.getDescription().isBlank())
                ? item.getDescription() : "(Không có mô tả)";
        Label descLabel = new Label(descText);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(364);
        descLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12; -fx-line-spacing: 2;");

        VBox descBox = new VBox(6, descTitle, descLabel);
        descBox.setPadding(new Insets(14, 28, 10, 28));

        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 20, 4, 20));

        // ── Auction posting section ──
        boolean hasOngoingAuction = sellerAuctions != null && sellerAuctions.stream()
                .anyMatch(a -> a.getItemId() == item.getId()
                        && ("ACTIVE".equalsIgnoreCase(a.getStatus())
                        ||  "UPCOMING".equalsIgnoreCase(a.getStatus())));

        Label auctionHeader = new Label("🏷  Đăng lên sàn đấu giá");
        auctionHeader.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #c2410c;");

        VBox auctionBox;
        if (hasOngoingAuction) {
            Label notice = new Label("⚠️  Sản phẩm này đang có phiên đấu giá chưa kết thúc. " +
                    "Bạn có thể tạo phiên mới sau khi phiên hiện tại hoàn thành hoặc bị hủy.");
            notice.setWrapText(true);
            notice.setMaxWidth(364);
            notice.setStyle("-fx-text-fill: #92400e; -fx-font-size: 11; -fx-line-spacing: 2;");
            auctionBox = new VBox(8, auctionHeader, notice);
        } else {
            Label priceHint = new Label("Giá khởi điểm (₫)");
            priceHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            TextField priceField = new TextField(String.format("%.0f", item.getPrice()));
            priceField.setStyle("-fx-background-radius: 6; -fx-border-color: #D1D5DB;" +
                    " -fx-border-radius: 6; -fx-padding: 7; -fx-font-size: 12;");

            Label startHint = new Label("Ngày bắt đầu");
            startHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            DatePicker dpStart = new DatePicker(java.time.LocalDate.now());
            dpStart.setMaxWidth(Double.MAX_VALUE);

            Label endHint = new Label("Ngày kết thúc");
            endHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            DatePicker dpEnd = new DatePicker();
            dpEnd.setPromptText("Chọn ngày kết thúc...");
            dpEnd.setMaxWidth(Double.MAX_VALUE);

            Label endTimeHint = new Label("Giờ kết thúc");
            endTimeHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            ObservableList<String> hours = FXCollections.observableArrayList();
            for (int h = 0; h < 24; h++) hours.add(String.format("%02d", h));
            ObservableList<String> minutes = FXCollections.observableArrayList("00", "15", "30", "45");
            ComboBox<String> cbHour = new ComboBox<>(hours);
            ComboBox<String> cbMin  = new ComboBox<>(minutes);
            cbHour.setValue("23"); cbMin.setValue("59");
            cbHour.setStyle("-fx-background-radius: 6;");
            cbMin.setStyle("-fx-background-radius: 6;");
            Label colonLabel = new Label(":");
            colonLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #374151;");
            HBox endTimeBox = new HBox(6, cbHour, colonLabel, cbMin);
            endTimeBox.setAlignment(Pos.CENTER_LEFT);

            Button btnPost = new Button("Đăng lên sàn  →");
            btnPost.setMaxWidth(Double.MAX_VALUE);
            btnPost.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; " +
                    "-fx-padding: 10; -fx-cursor: hand;");
            btnPost.setOnAction(e -> {
                try {
                    double price = Double.parseDouble(
                            priceField.getText().trim().replace(",", "").replace(".", ""));
                    java.time.LocalDate startDate = dpStart.getValue();
                    java.time.LocalDate endDate   = dpEnd.getValue();

                    if (startDate == null) { SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn ngày bắt đầu!"); return; }
                    if (endDate   == null) { SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn ngày kết thúc!"); return; }
                    if (startDate.isBefore(java.time.LocalDate.now())) { SceneUtil.showAlert("Ngày không hợp lệ", "Ngày bắt đầu không được là ngày trong quá khứ!"); return; }
                    if (!endDate.isAfter(startDate)) { SceneUtil.showAlert("Ngày không hợp lệ", "Ngày kết thúc phải sau ngày bắt đầu!"); return; }

                    int endHour = Integer.parseInt(cbHour.getValue());
                    int endMin  = Integer.parseInt(cbMin.getValue());
                    java.time.LocalDateTime endDateTime = endDate.atTime(endHour, endMin, 0);
                    if (!endDateTime.isAfter(startDate.atStartOfDay())) {
                        SceneUtil.showAlert("Giờ không hợp lệ", "Thời gian kết thúc phải sau thời gian bắt đầu!");
                        return;
                    }

                    String startTime = startDate.atStartOfDay().withNano(0).toString();
                    String endTime   = endDateTime.withNano(0).toString();
                    btnPost.setDisable(true);
                    btnPost.setText("Đang đăng...");

                    new Thread(() -> {
                        ApiResponse<Auction> resp = auctionApi.createAuction(
                                item.getId(), price, startTime, endTime);
                        Platform.runLater(() -> {
                            btnPost.setDisable(false);
                            btnPost.setText("Đăng lên sàn  →");
                            if (resp != null && resp.getStatus() == 201) {
                                popup.close();
                                SceneUtil.showAlert("Thành công",
                                        "\"" + item.getName() + "\" đã được đưa lên sàn đấu giá!");
                                if (onAuctionCreated != null) onAuctionCreated.run();
                            } else {
                                SceneUtil.showAlert("Đăng thất bại",
                                        resp != null ? resp.getMessage() : "Mất kết nối");
                            }
                        });
                    }).start();
                } catch (NumberFormatException ex) {
                    SceneUtil.showAlert("Lỗi nhập liệu", "Giá khởi điểm phải là số hợp lệ!");
                }
            });
            auctionBox = new VBox(8, auctionHeader,
                    priceHint, priceField, startHint, dpStart,
                    endHint, dpEnd, endTimeHint, endTimeBox, btnPost);
        }

        auctionBox.setPadding(new Insets(12, 28, 4, 28));
        auctionBox.setStyle("-fx-background-color: #fff7ed;");

        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(4, 20, 4, 20));

        // ── Edit button ──
        Button btnEdit = new Button("✏ Chỉnh sửa");
        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> {
            popup.close();
            if (onEditRequested != null) Platform.runLater(() -> onEditRequested.accept(item));
        });

        // ── Delete button ──
        Button btnDelete = new Button("🗑 Xóa");
        btnDelete.setDisable(hasOngoingAuction);
        btnDelete.setStyle("-fx-background-color: " + (hasOngoingAuction ? "#9ca3af" : "#ef4444") +
                "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 9 18; -fx-cursor: " + (hasOngoingAuction ? "default" : "hand") + ";");
        if (!hasOngoingAuction) {
            btnDelete.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận xóa");
                confirm.setHeaderText(null);
                confirm.setContentText("Bạn có chắc muốn xóa sản phẩm \"" + item.getName() + "\"?\n" +
                        "Hành động này không thể hoàn tác.");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp != ButtonType.OK) return;
                    new Thread(() -> {
                        ApiResponse<Void> res = itemApi.deleteItem(item.getId());
                        Platform.runLater(() -> {
                            popup.close();
                            if (res != null && res.getStatus() == 200) {
                                SceneUtil.showAlert("Đã xóa", "Sản phẩm \"" + item.getName() + "\" đã được xóa.");
                                if (onItemDeleted != null) onItemDeleted.accept(item);
                            } else {
                                SceneUtil.showAlert("Xóa thất bại",
                                        res != null ? res.getMessage() : "Mất kết nối");
                            }
                        });
                    }).start();
                });
            });
        }

        Button btnClose = new Button("← Quay lại");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; " +
                "-fx-padding: 9 18; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());

        HBox btnBar = new HBox(8, btnEdit, btnDelete, btnClose);
        btnBar.setAlignment(Pos.CENTER);
        btnBar.setPadding(new Insets(12, 20, 20, 20));

        ScrollPane scroll = new ScrollPane(
                new VBox(header, grid, descBox, sep1, auctionBox, sep2, btnBar));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scroll.setPrefHeight(620);

        popup.setScene(new Scene(scroll, 420, 620));
        popup.showAndWait();
    }
}

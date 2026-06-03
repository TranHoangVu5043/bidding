package Client.controller.user.dialogs;

import Client.model.auction.Auction;
import Client.networking.ApiResponse;
import Client.networking.endpoints.BidApi;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public final class AutoBidDialog {

    private AutoBidDialog() {}

    public static void show(Auction auction, BidApi bidApi) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("🤖 Đặt giá tự động – Phiên #" + auction.getId());
        dialog.setHeaderText(
                "Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()) + "\n" +
                "Hệ thống sẽ tự động tăng giá thay bạn cho đến khi đạt giá tối đa.");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(16, 24, 8, 24));

        Label lblMax = new Label("Giá tối đa (₫):");
        lblMax.setStyle("-fx-font-weight: bold;");
        TextField txtMax = new TextField();
        txtMax.setPromptText("Ví dụ: 5000000");
        txtMax.setPrefWidth(200);

        Label lblInc = new Label("Mức tăng mỗi lần (₫):");
        lblInc.setStyle("-fx-font-weight: bold;");
        TextField txtInc = new TextField();
        txtInc.setPromptText("Ví dụ: 100000");
        txtInc.setPrefWidth(200);

        Label lblNote = new Label("⚠ Số dư của bạn phải đủ để chi trả mức giá tối đa.");
        lblNote.setStyle("-fx-text-fill: #D97706; -fx-font-size: 11;");
        lblNote.setWrapText(true);
        lblNote.setMaxWidth(340);

        grid.add(lblMax,  0, 0); grid.add(txtMax, 1, 0);
        grid.add(lblInc,  0, 1); grid.add(txtInc, 1, 1);
        grid.add(lblNote, 0, 2, 2, 1);
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Xác nhận đặt giá tự động");
        okBtn.setStyle("-fx-background-color: #7C3AED; -fx-text-fill: white; -fx-font-weight: bold;");

        Platform.runLater(txtMax::requestFocus);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            try {
                double maxBid    = Double.parseDouble(txtMax.getText().replace(",", "").replace(".", "").trim());
                double increment = Double.parseDouble(txtInc.getText().replace(",", "").replace(".", "").trim());

                if (maxBid <= auction.getCurrentPrice()) {
                    showAlert(Alert.AlertType.WARNING, "Giá không hợp lệ",
                            String.format("Giá tối đa phải lớn hơn giá hiện tại (%,.0f ₫).",
                                    auction.getCurrentPrice()));
                    return;
                }
                if (increment <= 0) {
                    showAlert(Alert.AlertType.WARNING, "Mức tăng không hợp lệ",
                            "Mức tăng mỗi lần phải lớn hơn 0.");
                    return;
                }

                new Thread(() -> {
                    ApiResponse<Void> resp = bidApi.registerAutoBid(auction.getId(), maxBid, increment);
                    Platform.runLater(() -> {
                        if (resp != null && resp.getStatus() == 201) {
                            showAlert(Alert.AlertType.INFORMATION, "Đặt giá tự động thành công",
                                    String.format("Hệ thống sẽ tự động đặt giá cho phiên #%d\n" +
                                            "Giá tối đa: %,.0f ₫  |  Mức tăng: %,.0f ₫",
                                            auction.getId(), maxBid, increment));
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Thất bại",
                                    resp != null ? resp.getMessage() : "Mất kết nối tới Server");
                        }
                    });
                }).start();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                        "Vui lòng nhập số tiền hợp lệ (chỉ gồm các chữ số).");
            }
        });
    }

    private static void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

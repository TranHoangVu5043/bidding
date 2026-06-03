package Client.controller.user.dialogs;

import Client.networking.ApiResponse;
import Client.networking.endpoints.UserApi;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

public final class DepositDialog {

    private DepositDialog() {}

    public static void show(double currentBalance, UserApi userApi, Consumer<Double> onSuccess) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("💰 Nạp Tiền");
        dialog.setHeaderText(null);

        Label lblCurrent = new Label(String.format("Số dư hiện tại: %,.0f ₫", currentBalance));
        lblCurrent.setStyle("-fx-font-size: 13; -fx-text-fill: #374151; -fx-padding: 0 0 4 0;");

        Label lblHint = new Label("Chọn mệnh giá hoặc nhập số tiền tùy ý:");
        lblHint.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");

        TextField txtAmount = new TextField();
        txtAmount.setPromptText("Ví dụ: 500000");
        txtAmount.setStyle("-fx-background-radius: 8; -fx-padding: 9; -fx-font-size: 13;");

        double[] presets = {100_000, 500_000, 1_000_000, 5_000_000};
        String[] labels  = {"100.000 ₫", "500.000 ₫", "1.000.000 ₫", "5.000.000 ₫"};
        HBox presetRow = new HBox(8);
        for (int i = 0; i < presets.length; i++) {
            final double val = presets[i];
            Button btn = new Button(labels[i]);
            btn.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #0066CC; " +
                    "-fx-background-radius: 8; -fx-padding: 6 12; -fx-cursor: hand; -fx-font-size: 12;");
            btn.setOnAction(e -> txtAmount.setText(String.valueOf((long) val)));
            presetRow.getChildren().add(btn);
        }

        VBox content = new VBox(10, lblCurrent, new Separator(), lblHint, presetRow, txtAmount);
        content.setPadding(new Insets(16, 20, 4, 20));
        content.setPrefWidth(420);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Xác nhận nạp tiền");
        okBtn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        Platform.runLater(txtAmount::requestFocus);

        dialog.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.OK) return;
            String raw = txtAmount.getText().replace(",", "").replace(".", "").trim();
            double amount;
            try {
                amount = Double.parseDouble(raw);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Vui lòng nhập số tiền hợp lệ.");
                return;
            }
            if (amount <= 0) {
                showAlert(Alert.AlertType.WARNING, "Số tiền không hợp lệ", "Số tiền nạp phải lớn hơn 0.");
                return;
            }

            new Thread(() -> {
                ApiResponse<Double> resp = userApi.deposit(amount);
                Platform.runLater(() -> {
                    if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                        showAlert(Alert.AlertType.INFORMATION, "Nạp tiền thành công",
                                String.format("Đã nạp %,.0f ₫\nSố dư mới: %,.0f ₫", amount, resp.getData()));
                        if (onSuccess != null) onSuccess.accept(resp.getData());
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Nạp tiền thất bại",
                                resp != null ? resp.getMessage() : "Mất kết nối tới Server");
                    }
                });
            }).start();
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

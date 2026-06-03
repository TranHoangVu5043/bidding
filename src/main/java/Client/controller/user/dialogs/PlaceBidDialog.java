package Client.controller.user.dialogs;

import Client.model.auction.Auction;
import Client.networking.ApiResponse;
import Client.networking.endpoints.BidApi;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

public final class PlaceBidDialog {

    private PlaceBidDialog() {}

    public static void show(Auction auction, BidApi bidApi, Consumer<Double> onSuccess) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt giá – Phiên #" + auction.getId());
        dialog.setHeaderText("Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        dialog.setContentText("Nhập số tiền muốn đặt (₫):");

        dialog.showAndWait().ifPresent(input -> {
            try {
                double amount = Double.parseDouble(
                        input.replace(",", "").replace(".", "").trim());
                if (amount <= auction.getCurrentPrice()) {
                    showAlert(Alert.AlertType.WARNING, "Giá không hợp lệ",
                            String.format("Số tiền phải lớn hơn giá hiện tại (%,.0f ₫).",
                                    auction.getCurrentPrice()));
                    return;
                }

                new Thread(() -> {
                    ApiResponse<Double> resp = bidApi.placeBid(auction.getId(), amount);
                    Platform.runLater(() -> {
                        if (resp != null && resp.getStatus() == 201) {
                            showAlert(Alert.AlertType.INFORMATION, "Đặt giá thành công",
                                    String.format("Bạn đã đặt giá %,.0f ₫ thành công!", amount));
                            if (onSuccess != null && resp.getData() != null)
                                onSuccess.accept(resp.getData());
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Đặt giá thất bại",
                                    resp != null ? resp.getMessage() : "Mất kết nối tới Server");
                        }
                    });
                }).start();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                        "Vui lòng nhập một số tiền hợp lệ (ví dụ: 1500000).");
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

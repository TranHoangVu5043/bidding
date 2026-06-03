package Client.controller.user.dialogs;

import Client.model.auction.Auction;
import Client.model.auction.Bid;
import Client.networking.ApiResponse;
import Client.networking.endpoints.BidApi;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public final class BidHistoryDialog {

    private BidHistoryDialog() {}

    public static void show(Auction auction, BidApi bidApi) {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Lượt đặt");
        xAxis.setTickLabelRotation(30);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số tiền (₫)");
        yAxis.setForceZeroInRange(false);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle("Lịch sử giá đặt – Phiên #" + auction.getId());
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setLegendVisible(false);
        chart.setPrefWidth(640);
        chart.setPrefHeight(360);
        VBox.setVgrow(chart, Priority.ALWAYS);

        Label placeholder = new Label("⏳ Đang tải...");
        placeholder.setStyle("-fx-text-fill: #555; -fx-font-size: 13;");

        VBox content = new VBox(10, placeholder);
        content.setPadding(new Insets(10));
        content.setPrefHeight(400);
        VBox.setVgrow(content, Priority.ALWAYS);

        Label header = new Label("Phiên #" + auction.getId()
                + "  |  Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        header.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1d4ed8;" +
                " -fx-padding: 8 0 4 0;");

        Button btnClose = new Button("Đóng");
        btnClose.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-background-radius: 6; -fx-padding: 6 20;");

        HBox footer = new HBox(btnClose);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(8, 10, 8, 10));

        VBox root = new VBox(0, header, content, footer);
        root.setPadding(new Insets(10, 14, 4, 14));

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Lịch sử đấu giá – Phiên #" + auction.getId());
        stage.setMinWidth(680);
        stage.setMinHeight(480);
        stage.setScene(new Scene(root, 680, 480));

        btnClose.setOnAction(e -> stage.close());

        // Load bid history off the UI thread
        new Thread(() -> {
            ApiResponse<List<Bid>> resp = bidApi.getBidHistory(auction.getId());
            Platform.runLater(() -> {
                content.getChildren().remove(placeholder);
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    List<Bid> bids = resp.getData();
                    if (bids.isEmpty()) {
                        content.getChildren().add(new Label("Chưa có lượt đặt giá nào."));
                        return;
                    }

                    List<Bid> sorted = bids.stream()
                            .filter(b -> b.getCreatedAt() != null)
                            .sorted(java.util.Comparator.comparing(Bid::getCreatedAt))
                            .toList();

                    double minAmt = sorted.stream().mapToDouble(Bid::getAmount).min().orElse(0);
                    double maxAmt = sorted.stream().mapToDouble(Bid::getAmount).max().orElse(1);
                    double pad    = Math.max((maxAmt - minAmt) * 0.15, maxAmt * 0.05);
                    yAxis.setAutoRanging(false);
                    yAxis.setLowerBound(Math.max(0, minAmt - pad));
                    yAxis.setUpperBound(maxAmt + pad);
                    yAxis.setTickUnit(Math.max(1, (maxAmt - minAmt + 2 * pad) / 6));

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    for (int i = 0; i < sorted.size(); i++) {
                        Bid b    = sorted.get(i);
                        String name = b.getUsername() != null ? b.getUsername() : "User#" + b.getUserId();
                        String time = formatBidTime(b.getCreatedAt(), i + 1);
                        series.getData().add(new XYChart.Data<>("#" + (i + 1) + " " + name + " " + time, b.getAmount()));
                    }
                    chart.getData().add(series);
                    content.getChildren().add(chart);
                } else {
                    String msg = resp != null ? resp.getMessage() : "Mất kết nối";
                    content.getChildren().add(new Label("Không thể tải lịch sử: " + msg));
                }
            });
        }).start();

        stage.showAndWait();
    }

    private static String formatBidTime(String createdAt, int index) {
        if (createdAt == null) return "#" + index;
        int t = createdAt.indexOf('T');
        if (t >= 0 && createdAt.length() > t + 8) return createdAt.substring(t + 1, t + 9);
        return "#" + index;
    }
}

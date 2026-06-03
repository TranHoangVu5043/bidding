package Client.controller.seller.helpers;

import Client.model.auction.Auction;
import Client.model.item.Item;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public class SellerChartHelper {

    private final LineChart<String, Number>  weekChart;
    private final NumberAxis                 weekYAxis;
    private final PieChart                   pieChart;
    private final VBox                       pieLegend;
    private final AreaChart<String, Number>  areaChart;
    private final NumberAxis                 areaYAxis;
    private final VBox                       finishedVBox;

    private final ObservableList<Auction> sellerAuctions;
    private final ObservableList<Item>    masterData;

    private static final String[][] PIE_LEGEND = {
            {"Điện tử",    "#3B82F6"},
            {"Nghệ thuật", "#EC4899"},
            {"Xe cộ",      "#10B981"},
    };

    public SellerChartHelper(
            LineChart<String, Number>  weekChart,
            NumberAxis                 weekYAxis,
            PieChart                   pieChart,
            VBox                       pieLegend,
            AreaChart<String, Number>  areaChart,
            NumberAxis                 areaYAxis,
            VBox                       finishedVBox,
            ObservableList<Auction>    sellerAuctions,
            ObservableList<Item>       masterData) {

        this.weekChart      = weekChart;
        this.weekYAxis      = weekYAxis;
        this.pieChart       = pieChart;
        this.pieLegend      = pieLegend;
        this.areaChart      = areaChart;
        this.areaYAxis      = areaYAxis;
        this.finishedVBox   = finishedVBox;
        this.sellerAuctions = sellerAuctions;
        this.masterData     = masterData;
    }

    //  Week revenue LineChart 

    public void setupWeekRevenueChart() {
        if (weekChart == null) return;

        java.time.LocalDate today    = java.time.LocalDate.now();
        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        String[] labels   = new String[7];
        double[] values   = new double[7];

        for (int i = 0; i < 7; i++) {
            java.time.LocalDate day = today.minusDays(6 - i);
            labels[i] = dayNames[day.getDayOfWeek().getValue() % 7];
            values[i] = 0;
        }

        for (Auction a : sellerAuctions) {
            if (!"FINISHED".equalsIgnoreCase(a.getStatus()) || a.getEndTime() == null) continue;
            try {
                java.time.LocalDate endDate = java.time.LocalDateTime
                        .parse(a.getEndTime().replace(" ", "T")).toLocalDate();
                long daysAgo = today.toEpochDay() - endDate.toEpochDay();
                if (daysAgo >= 0 && daysAgo < 7) values[(int)(6 - daysAgo)] += a.getCurrentPrice();
            } catch (Exception ignored) {}
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) series.getData().add(new XYChart.Data<>(labels[i], values[i]));
        weekChart.getData().clear();
        weekChart.getData().add(series);

        if (weekYAxis != null) {
            weekYAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                @Override public String toString(Number n) {
                    double v = n.doubleValue();
                    if (v >= 1_000_000) return String.format("%.0fM", v / 1_000_000);
                    if (v >= 1_000)     return String.format("%.0fK", v / 1_000);
                    return String.format("%.0f", v);
                }
                @Override public Number fromString(String s) { return 0; }
            });
        }
    }

    //  Category PieChart ─

    public void setupCategoryPieChart() {
        if (pieChart == null) return;

        long electronics = masterData.stream().filter(i -> "ELECTRONICS".equalsIgnoreCase(i.getCategory())).count();
        long art         = masterData.stream().filter(i -> "ART".equalsIgnoreCase(i.getCategory())).count();
        long vehicle     = masterData.stream().filter(i -> "VEHICLE".equalsIgnoreCase(i.getCategory())).count();
        long total       = electronics + art + vehicle;
        long[] counts    = {electronics, art, vehicle};

        javafx.collections.ObservableList<PieChart.Data> pieData =
                javafx.collections.FXCollections.observableArrayList(
                        new PieChart.Data("Điện tử ("    + electronics + ")", electronics),
                        new PieChart.Data("Nghệ thuật (" + art         + ")", art),
                        new PieChart.Data("Xe cộ ("      + vehicle     + ")", vehicle));

        pieChart.setData(pieData);

        String[] colors = {"#3B82F6", "#EC4899", "#10B981"};
        for (int i = 0; i < pieData.size(); i++) {
            pieData.get(i).getNode().setStyle("-fx-pie-color: " + colors[i] + ";");
        }

        if (pieLegend != null) {
            pieLegend.getChildren().clear();
            for (int i = 0; i < PIE_LEGEND.length; i++) {
                String lbl   = PIE_LEGEND[i][0];
                String color = PIE_LEGEND[i][1];
                long   cnt   = counts[i];
                double pct   = total > 0 ? (cnt * 100.0 / total) : 0;

                Rectangle dot = new Rectangle(10, 10);
                dot.setArcWidth(3); dot.setArcHeight(3);
                dot.setFill(Color.web(color));

                Label lblName = new Label(lbl);
                lblName.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
                HBox.setHgrow(lblName, Priority.ALWAYS);

                Label lblVal = new Label(String.format("%,.0f sp  •  %.0f%%", (double) cnt, pct));
                lblVal.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

                HBox row = new HBox(8, dot, lblName, lblVal);
                row.setAlignment(Pos.CENTER_LEFT);
                pieLegend.getChildren().add(row);
            }
        }
    }

    //  Revenue AreaChart + finished-auctions table ─

    public void renderRevenueData(List<Auction> auctions) {
        if (finishedVBox != null) {
            finishedVBox.getChildren().clear();
            if (auctions == null || auctions.isEmpty()) {
                Label empty = new Label("Chưa có doanh thu từ phiên đấu giá nào.");
                empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
                finishedVBox.getChildren().add(empty);
            } else {
                for (Auction a : auctions) finishedVBox.getChildren().add(buildHistoryRow(a));
            }
        }

        if (areaChart == null) return;

        double[] monthly = new double[12];
        for (Auction a : auctions) {
            try {
                if (a.getEndTime() != null) {
                    java.time.LocalDateTime dt = java.time.LocalDateTime.parse(a.getEndTime().replace(" ", "T"));
                    monthly[dt.getMonthValue() - 1] += a.getCurrentPrice();
                }
            } catch (Exception ignored) {}
        }

        String[] monthLabels = {"T1","T2","T3","T4","T5","T6","T7","T8","T9","T10","T11","T12"};
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 12; i++) series.getData().add(new XYChart.Data<>(monthLabels[i], monthly[i]));
        areaChart.getData().clear();
        areaChart.getData().add(series);

        if (areaYAxis != null) {
            areaYAxis.setTickLabelFormatter(new javafx.util.StringConverter<Number>() {
                @Override public String toString(Number n) {
                    double v = n.doubleValue();
                    if (v >= 1_000_000) return String.format("%.0fM", v / 1_000_000);
                    if (v >= 1_000)     return String.format("%.0fK", v / 1_000);
                    return String.format("%.0f", v);
                }
                @Override public Number fromString(String s) { return 0; }
            });
        }
    }

    public HBox buildHistoryRow(Auction a) {
        Label idLabel = new Label("Phiên #" + a.getId());
        idLabel.setPrefWidth(80);
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1e293b;");

        String itemDisplay = (a.getItemName() != null && !a.getItemName().isBlank())
                ? a.getItemName() : "Mặt hàng #" + a.getItemId();
        Label itemLabel = new Label(itemDisplay);
        itemLabel.setPrefWidth(200);
        itemLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");

        Label priceLabel = new Label(String.format("%,.0f ₫", a.getCurrentPrice()));
        priceLabel.setPrefWidth(150);
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #16a34a;");

        Label endLabel = new Label(formatTime(a.getEndTime()));
        endLabel.setPrefWidth(140);
        endLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        HBox row = new HBox(0, idLabel, itemLabel, priceLabel, endLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
        return row;
    }

    private String formatTime(String t) {
        if (t == null) return "—";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(t.replace(" ", "T"));
            return String.format("%02d/%02d/%d %02d:%02d",
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(), dt.getHour(), dt.getMinute());
        } catch (Exception e) { return t; }
    }
}

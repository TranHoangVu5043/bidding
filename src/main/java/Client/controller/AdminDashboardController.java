package Client.controller;

import Client.model.ActivityLog;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminDashboardController {

    // ── Sidebar buttons (fx:id khớp với Adminview.fxml) ──
    @FXML private Button btnHome;
    @FXML private Button btnUsers;
    @FXML private Button btnSellers;
    @FXML private Button btnInventory;
    @FXML private Button btnOrders;
    @FXML private Button btnAuctions;
    @FXML private Button btnAnalytics;
    @FXML private Button btnNotifications;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;

    // ── Labels ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblPageTitle;

    // ── Chart ──
    @FXML private LineChart<String, Number> chartRevenue; // FIX: chartRevenueTrend → chartRevenue

    // ── Activity table ──
    @FXML private TableView<ActivityLog> tableActivity;
    @FXML private TableColumn<ActivityLog, String> colActUser;    // FIX: colUser → colActUser
    @FXML private TableColumn<ActivityLog, String> colActAction;  // FIX: colActivity → colActAction
    @FXML private TableColumn<ActivityLog, String> colActTime;
    @FXML private TableColumn<ActivityLog, String> colActStatus;  // FIX: colStatus → colActStatus

    // ── TabPane để chuyển tab khi bấm sidebar ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabUsers;
    @FXML private Tab tabSellers;
    @FXML private Tab tabInventory;
    @FXML private Tab tabOrders;
    @FXML private Tab tabAuctions;
    @FXML private Tab tabAnalytics;
    @FXML private Tab tabNotifications;
    @FXML private Tab tabSettings;

    @FXML
    public void initialize() {
        lblTotalRevenue.setText("1,337,000,000 ₫");
        setupChart();
        setupTable();
    }

    /*private void setupTable() {
        // FIX: property name phải khớp với getter trong ActivityLog
        colActUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colActAction.setCellValueFactory(new PropertyValueFactory<>("action"));     // FIX: "activity" → "action"
        colActTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colActStatus.setCellValueFactory(new PropertyValueFactory<>("timestamp")); // placeholder, thay bằng field thật khi có
    }*/

    private void setupChart() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        series.getData().add(new XYChart.Data<>("T1", 10));
        series.getData().add(new XYChart.Data<>("T2", 40));
        series.getData().add(new XYChart.Data<>("T3", 30));
        series.getData().add(new XYChart.Data<>("T4", 60));
        series.getData().add(new XYChart.Data<>("T5", 20));
        chartRevenue.getData().add(series);
    }

    // ── Sidebar navigation: mỗi handler chuyển đúng tab thay vì chỉ print ──

    @FXML
    private void handleHome() {
        switchTab(tabDashboard, "Dashboard");
        highlightButton(btnHome);
    }

    @FXML
    private void handleUsers() {
        switchTab(tabUsers, "Quản Lý Người Dùng");
        highlightButton(btnUsers);
    }
    @FXML
    private void handleAddProduct() {}

    @FXML
    private void handleSellers() {
        switchTab(tabSellers, "Quản Lý Người Bán");
        highlightButton(btnSellers);
    }

    @FXML
    private void handleInventory() {
        switchTab(tabInventory, "Inventory");
        highlightButton(btnInventory);
    }

    @FXML
    private void handleOrders() {
        switchTab(tabOrders, "Đơn Hàng");
        highlightButton(btnOrders);
    }

    @FXML
    private void handleAuctions() {
        switchTab(tabAuctions, "Đấu Giá");
        highlightButton(btnAuctions);
    }

    @FXML
    private void handleAnalytics() {
        switchTab(tabAnalytics, "Phân Tích");
        highlightButton(btnAnalytics);
    }

    @FXML
    private void handleNotifications() {
        switchTab(tabNotifications, "Thông Báo");
        highlightButton(btnNotifications);
    }

    @FXML
    private void handleSettings() {
        switchTab(tabSettings, "Cài Đặt");
        highlightButton(btnSettings);
    }

    @FXML
    private void handleSignOut() {
        System.exit(0);
    }

    // ── Các handler còn lại khai báo trong FXML ──
    @FXML private void handleExportUsers() {}
    @FXML private void handleBanUser() {}
    @FXML private void handleApproveSeller() {}
    @FXML private void handleCreateAuction() {}
    @FXML private void handleRefreshAnalytics() {}
    @FXML private void handleSendNotification() {}
    @FXML private void handleSavePlatform() {}
    @FXML private void handleChangeAdminPw() {}

    // ── Helpers ──

    private void switchTab(Tab tab, String title) {
        mainTabPane.getSelectionModel().select(tab);
        lblPageTitle.setText(title);
    }

    private void highlightButton(Button active) {
        // Reset tất cả về transparent
        for (Button b : new Button[]{btnHome, btnUsers, btnSellers, btnInventory,
                btnOrders, btnAuctions, btnAnalytics, btnNotifications, btnSettings}) {
            b.setStyle(b.getStyle()
                    .replace("-fx-background-color: #2ecc71;", "-fx-background-color: transparent;")
                    .replace("-fx-text-fill: white;", "-fx-text-fill: #CBD5E1;"));
        }
        // Highlight nút đang chọn
        active.setStyle(active.getStyle()
                .replace("-fx-background-color: transparent;", "-fx-background-color: #2ecc71;")
                .replace("-fx-text-fill: #CBD5E1;", "-fx-text-fill: white;"));
    }

    private void setupTable() {
        colActUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colActAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colActTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colActStatus.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        // Thêm data mẫu trực tiếp
        ObservableList<ActivityLog> data = FXCollections.observableArrayList(
                new ActivityLog("admin",   "Đăng nhập",     "2024-01-01 08:00", "SUCCESS"),
                new ActivityLog("seller1", "Thêm sản phẩm", "2024-01-01 09:00", "SUCCESS")
        );
        tableActivity.setItems(data);
    }
}
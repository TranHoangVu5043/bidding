package Client.controller;

import Client.model.Auction;
import Client.model.ActivityLog;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.UserApi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AdminDashboardController {

    // ── Sidebar buttons ──
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

    // ── Dashboard labels ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalSellers;
    @FXML private Label lblTotalAuctions;
    @FXML private Label lblPageTitle;

    // ── Chart ──
    @FXML private LineChart<String, Number> chartRevenue;

    // ── Activity table ──
    @FXML private TableView<ActivityLog>           tableActivity;
    @FXML private TableColumn<ActivityLog, String> colActUser;
    @FXML private TableColumn<ActivityLog, String> colActAction;
    @FXML private TableColumn<ActivityLog, String> colActTime;
    @FXML private TableColumn<ActivityLog, String> colActStatus;

    // ── TabPane ──
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

    // ── Auction tab ──
    @FXML private TextField                    txtAuctionSearch;
    @FXML private ComboBox<String>             cmbAuctionStatus;
    @FXML private TableView<Auction>           tableAuctions;
    @FXML private TableColumn<Auction, Integer> colAucId;
    @FXML private TableColumn<Auction, Integer> colAucProduct;
    @FXML private TableColumn<Auction, Integer> colAucSeller;
    @FXML private TableColumn<Auction, Double>  colAucStart;
    @FXML private TableColumn<Auction, Double>  colAucCurrent;
    @FXML private TableColumn<Auction, String>  colAucEndTime;
    @FXML private TableColumn<Auction, String>  colAucStatus;

    // ── API ──
    private final AuctionApi auctionApi = new AuctionApi();
    private final UserApi    userApi    = new UserApi();

    // ── Data ──
    private final ObservableList<Auction>   allAuctions      = FXCollections.observableArrayList();
    private FilteredList<Auction>           filteredAuctions;
    private final ObservableList<ActivityLog> activityLogs   = FXCollections.observableArrayList();

    // ══════════════════════════════════════════
    // Initialize
    // ══════════════════════════════════════════
    @FXML
    public void initialize() {
        setupAuctionTable();
        setupActivityTable();
        setupChart();
        setupComboBoxes();
        loadAuctions();
    }

    // ══════════════════════════════════════════
    // Setup
    // ══════════════════════════════════════════
    private void setupAuctionTable() {
        if (tableAuctions == null) return;

        colAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAucProduct.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colAucSeller.setCellValueFactory(new PropertyValueFactory<>("ownerId"));
        colAucStart.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colAucCurrent.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colAucEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colAucStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredAuctions = new FilteredList<>(allAuctions, p -> true);
        tableAuctions.setItems(filteredAuctions);

        // Thêm nút "Dừng" trong cột colAucAction nếu cần (dùng cell factory)
        // Hiện tại xử lý qua nút riêng khi chọn dòng
    }

    private void setupActivityTable() {
        if (tableActivity == null) return;

        colActUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colActAction.setCellValueFactory(new PropertyValueFactory<>("action"));
        colActTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        colActStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        activityLogs.setAll(
                new ActivityLog("admin",   "Đăng nhập",     "2026-01-01 08:00", "SUCCESS"),
                new ActivityLog("seller1", "Thêm sản phẩm", "2026-01-01 09:00", "SUCCESS")
        );
        tableActivity.setItems(activityLogs);
    }

    private void setupChart() {
        if (chartRevenue == null) return;
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");
        series.getData().add(new XYChart.Data<>("T1", 10));
        series.getData().add(new XYChart.Data<>("T2", 40));
        series.getData().add(new XYChart.Data<>("T3", 30));
        series.getData().add(new XYChart.Data<>("T4", 60));
        series.getData().add(new XYChart.Data<>("T5", 20));
        chartRevenue.getData().add(series);
    }

    private void setupComboBoxes() {
        if (cmbAuctionStatus != null) {
            cmbAuctionStatus.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "UPCOMING", "ENDED", "CANCELLED"));
            cmbAuctionStatus.valueProperty().addListener((obs, o, n) -> applyAuctionFilter());
        }
        if (txtAuctionSearch != null) {
            txtAuctionSearch.textProperty().addListener((obs, o, n) -> applyAuctionFilter());
        }
    }

    // ══════════════════════════════════════════
    // Load danh sách auction từ server
    // ══════════════════════════════════════════
    private void loadAuctions() {
        new Thread(() -> {
            ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    allAuctions.setAll(res.getData());
                    updateDashboardStats();
                } else {
                    showAlert("Lỗi", "Không thể tải danh sách đấu giá: " + res.getMessage());
                }
            });
        }).start();
    }

    private void updateDashboardStats() {
        long active = allAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
        double revenue = allAuctions.stream()
                .filter(a -> "ENDED".equalsIgnoreCase(a.getStatus()))
                .mapToDouble(Auction::getCurrentPrice).sum();

        if (lblTotalAuctions != null)
            lblTotalAuctions.setText(active + " đang chạy");
        if (lblTotalRevenue != null)
            lblTotalRevenue.setText(String.format("%,.0f ₫", revenue));
    }

    // ══════════════════════════════════════════
    // Lọc auction
    // ══════════════════════════════════════════
    private void applyAuctionFilter() {
        String keyword = txtAuctionSearch != null ? txtAuctionSearch.getText().toLowerCase().trim() : "";
        String status  = cmbAuctionStatus != null ? cmbAuctionStatus.getValue() : null;

        filteredAuctions.setPredicate(a -> {
            boolean matchKw = keyword.isEmpty()
                    || String.valueOf(a.getId()).contains(keyword)
                    || String.valueOf(a.getItemId()).contains(keyword);
            boolean matchStatus = status == null || "Tất cả".equals(status)
                    || status.equalsIgnoreCase(a.getStatus());
            return matchKw && matchStatus;
        });
    }

    // ══════════════════════════════════════════
    // Auction — Hủy phiên (Admin có thể dừng bất kỳ phiên nào)
    // ══════════════════════════════════════════


    // Được gọi từ nút trong bảng hoặc nút toolbar — dừng phiên được chọn
    @FXML
    private void handleCancelSelectedAuction() {
        if (tableAuctions == null) return;
        Auction selected = tableAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn một phiên đấu giá trong bảng.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận dừng phiên");
        confirm.setHeaderText(null);
        confirm.setContentText("Dừng phiên đấu giá #" + selected.getId() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    ApiResponse<Void> res = auctionApi.cancelAuction(selected.getId());
                    Platform.runLater(() -> {
                        if (res.getStatus() == 200) {
                            showAlert("Thành công", "Đã dừng phiên #" + selected.getId());
                            loadAuctions();
                        } else {
                            showAlert("Thất bại", "Không thể dừng phiên: " + res.getMessage());
                        }
                    });
                }).start();
            }
        });
    }

    // ══════════════════════════════════════════
    // Sidebar handlers
    // ══════════════════════════════════════════
    @FXML private void handleHome()          { switchTab(tabDashboard,     "Dashboard");            highlightButton(btnHome);          loadAuctions(); }
    @FXML private void handleUsers()         { switchTab(tabUsers,          "Quản Lý Người Dùng");  highlightButton(btnUsers); }
    @FXML private void handleSellers()       { switchTab(tabSellers,        "Quản Lý Người Bán");   highlightButton(btnSellers); }
    @FXML private void handleInventory()     { switchTab(tabInventory,      "Inventory");            highlightButton(btnInventory); }
    @FXML private void handleOrders()        { switchTab(tabOrders,         "Đơn Hàng");             highlightButton(btnOrders); }
    @FXML private void handleAuctions()      { switchTab(tabAuctions,       "Đấu Giá");              highlightButton(btnAuctions);      loadAuctions(); }
    @FXML private void handleAnalytics()     { switchTab(tabAnalytics,      "Phân Tích");            highlightButton(btnAnalytics); }
    @FXML private void handleNotifications() { switchTab(tabNotifications,  "Thông Báo");            highlightButton(btnNotifications); }
    @FXML private void handleSettings()      { switchTab(tabSettings,       "Cài Đặt");              highlightButton(btnSettings); }

    @FXML
    private void handleSignOut() {
        new Thread(() -> {
            userApi.logout();
            Platform.runLater(Platform::exit);
        }).start();
    }

    // ══════════════════════════════════════════
    // Stub handlers (FXML yêu cầu khai báo)
    // ══════════════════════════════════════════
    @FXML private void handleAddProduct()        { /* TODO */ }
    @FXML private void handleExportUsers()       { /* TODO */ }
    @FXML private void handleBanUser()           { /* TODO */ }
    @FXML private void handleApproveSeller()     { /* TODO */ }
    @FXML private void handleRefreshAnalytics()  { loadAuctions(); }
    @FXML private void handleSendNotification()  { /* TODO */ }
    @FXML private void handleSavePlatform()      { /* TODO */ }
    @FXML private void handleChangeAdminPw()     { /* TODO */ }

    // ══════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════
    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null) lblPageTitle.setText(title);
    }

    private void highlightButton(Button active) {
        Button[] all = {btnHome, btnUsers, btnSellers, btnInventory,
                btnOrders, btnAuctions, btnAnalytics, btnNotifications, btnSettings};
        for (Button b : all) {
            if (b == null) continue;
            b.getStyleClass().remove("sidebar-active");
        }
        if (active != null) active.getStyleClass().add("sidebar-active");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

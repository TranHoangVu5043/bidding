
package Client.controller.admin;

import Client.model.admin.ActivityLog;
import Client.model.user.User;
import Client.networking.SessionManager;
import Client.util.SceneUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

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

    // ── Labels ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblPageTitle;
    @FXML private Label welcomeLabel;   // sidebar admin name (fx:id="welcomeLabel" in Adminview.fxml)

    // ── Chart ──
    @FXML private LineChart<String, Number> chartRevenue;

    // ── Activity table ──
    @FXML private TableView<ActivityLog>        tableActivity;
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

    private Button[] sidebarButtons;

    @FXML
    public void initialize() {
        sidebarButtons = new Button[]{
            btnHome, btnUsers, btnSellers, btnInventory,
            btnOrders, btnAuctions, btnAnalytics, btnNotifications, btnSettings
        };

        lblTotalRevenue.setText("1,337,000,000 ₫");
        lblPageTitle.setText("Dashboard");
        setupChart();
        setupTable();
        highlightButton(btnHome);
        populateAdminInfo();
    }

    private void populateAdminInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null || welcomeLabel == null) return;
        welcomeLabel.setText(user.getUsername() != null ? user.getUsername() : "Admin");
    }

    // ── Table setup ──
    private void setupTable() {
        colActUser.setCellValueFactory(new PropertyValueFactory<>("user"));
        colActAction.setCellValueFactory(new PropertyValueFactory<>("activity")); // matches getActivity()
        colActTime.setCellValueFactory(new PropertyValueFactory<>("time"));
        colActStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        ObservableList<ActivityLog> data = FXCollections.observableArrayList(
                new ActivityLog("admin",   "Đăng nhập",     "2024-01-01 08:00", "SUCCESS"),
                new ActivityLog("seller1", "Thêm sản phẩm", "2024-01-01 09:00", "SUCCESS")
        );
        tableActivity.setItems(data);
    }

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

    // ── Sidebar navigation ──

    @FXML private void handleHome()          { switchTab(tabDashboard,     "Dashboard");            highlightButton(btnHome); }
    @FXML private void handleUsers()         { switchTab(tabUsers,         "Quản Lý Người Dùng");  highlightButton(btnUsers); }
    @FXML private void handleSellers()       { switchTab(tabSellers,       "Quản Lý Người Bán");   highlightButton(btnSellers); }
    @FXML private void handleInventory()     { switchTab(tabInventory,     "Quản Lý Sản Phẩm");    highlightButton(btnInventory); }
    @FXML private void handleOrders()        { switchTab(tabOrders,        "Đơn Hàng");             highlightButton(btnOrders); }
    @FXML private void handleAuctions()      { switchTab(tabAuctions,      "Đấu Giá");              highlightButton(btnAuctions); }
    @FXML private void handleAnalytics()     { switchTab(tabAnalytics,     "Phân Tích");            highlightButton(btnAnalytics); }
    @FXML private void handleNotifications() { switchTab(tabNotifications, "Thông Báo");            highlightButton(btnNotifications); }
    @FXML private void handleSettings()      { switchTab(tabSettings,      "Cài Đặt");              highlightButton(btnSettings); }

    @FXML
    private void handleSignOut() {
        // FIX: clear session and navigate to login instead of System.exit(0)
        SessionManager.clear();
        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
    }

    // ── Other FXML handlers ──
    @FXML private void handleAddProduct()        {}
    @FXML private void handleExportUsers()       {}
    @FXML private void handleBanUser()           {}
    @FXML private void handleApproveSeller()     {}
    @FXML private void handleCreateAuction()     {}
    @FXML private void handleRefreshAnalytics()  {}
    @FXML private void handleSendNotification()  {}
    @FXML private void handleSavePlatform()      {}
    @FXML private void handleChangeAdminPw()     {}

    // ── Helpers ──

    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) {
            mainTabPane.getSelectionModel().select(tab);
        }
        if (lblPageTitle != null && title != null) {
            lblPageTitle.setText(title);
        }
    }

    private void highlightButton(Button active) {
        if (sidebarButtons == null) return;
        for (Button b : sidebarButtons) {
            if (b == null) continue;
            b.getStyleClass().remove("sidebar-active");
            b.setStyle(b.getStyle()
                    .replace("-fx-background-color: #2ecc71;", "-fx-background-color: transparent;")
                    .replace("-fx-text-fill: white;", "-fx-text-fill: #CBD5E1;"));
        }
        if (active != null) {
            active.setStyle(active.getStyle()
                    .replace("-fx-background-color: transparent;", "-fx-background-color: #2ecc71;")
                    .replace("-fx-text-fill: #CBD5E1;", "-fx-text-fill: white;"));
        }
    }
}


package Client.controller.admin;

import Client.model.user.User;
import Client.networking.SessionManager;
import Client.util.SceneUtil;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class AdminDashboardController {

    // ── Sidebar buttons ──
    @FXML private Button btnHome;
    @FXML private Button btnUsers;
    @FXML private Button btnSellers;
    @FXML private Button btnInventory;
    @FXML private Button btnOrders;
    @FXML private Button btnAuctions;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;

    // ── Labels ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblPageTitle;
    @FXML private Label welcomeLabel;   

    // ── Chart ──
    @FXML private LineChart<String, Number> chartRevenue;

    // ── Activity feed ──
    @FXML private VBox activityVBox;

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
            btnOrders, btnAuctions, btnAnalytics, btnSettings
        };

        lblTotalRevenue.setText("1,337,000,000 ₫");
        lblPageTitle.setText("Dashboard");
        setupChart();
        populateActivityFeed();
        highlightButton(btnHome);
        populateAdminInfo();
    }

    private void populateAdminInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null || welcomeLabel == null) return;
        welcomeLabel.setText(user.getUsername() != null ? user.getUsername() : "Admin");
    }

    // ── Activity feed ──
    private void populateActivityFeed() {
        if (activityVBox == null) return;
        activityVBox.getChildren().clear();
        addActivityRow("admin",   "Đăng nhập",     "2024-01-01 08:00", "SUCCESS");
        addActivityRow("seller1", "Thêm sản phẩm", "2024-01-01 09:00", "SUCCESS");
    }

    private void addActivityRow(String user, String action, String time, String status) {
        Label userLbl = new Label(user);
        userLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-pref-width: 100;");
        Label actionLbl = new Label(action);
        actionLbl.setStyle("-fx-font-size: 12;");
        HBox.setHgrow(actionLbl, Priority.ALWAYS);
        Label timeLbl = new Label(time);
        timeLbl.setStyle("-fx-font-size: 11; -fx-text-fill: #9ca3af; -fx-pref-width: 140;");
        String sColor = "SUCCESS".equals(status) ? "#16a34a" : "#ef4444";
        Label statusLbl = new Label(status);
        statusLbl.setStyle("-fx-background-color: " + sColor + "; -fx-text-fill: white;" +
                           "-fx-font-size: 10; -fx-padding: 2 7; -fx-background-radius: 4;");
        HBox row = new HBox(12, userLbl, actionLbl, timeLbl, statusLbl);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8;");
        activityVBox.getChildren().add(row);
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

    @FXML private void handleHome()          { switchTab(tabDashboard,     "Dashboard", btnHome );            highlightButton(btnHome); }
    @FXML private void handleUsers()         { switchTab(tabUsers,         "Quản Lý Người Dùng", btnUsers);  highlightButton(btnUsers); }
    @FXML private void handleSellers()       { switchTab(tabSellers,       "Quản Lý Người Bán",btnSellers);   highlightButton(btnSellers); }
    @FXML private void handleInventory()     { switchTab(tabInventory,     "Quản Lý Sản Phẩm",btnInventory);    highlightButton(btnInventory); }
    @FXML private void handleOrders()        { switchTab(tabOrders,        "Đơn Hàng",btnOrders);             highlightButton(btnOrders); }
    @FXML private void handleAuctions()      { switchTab(tabAuctions,      "Đấu Giá",btnAuctions);              highlightButton(btnAuctions); }
    @FXML private void handleAnalytics()     { switchTab(tabAnalytics,     "Phân Tích",btnAnalytics);            highlightButton(btnAnalytics); }
    @FXML private void handleSettings()      { switchTab(tabSettings,      "Cài Đặt",btnSettings);              highlightButton(btnSettings); }

    @FXML
    private void handleSignOut() {
        SessionManager.clear();
        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
    }

    // ── Other FXML handlers ──
    @FXML private void handleAddProduct()        {}
    @FXML private void handleExportUsers()       {}{}
    @FXML private void handleApproveSeller()     {}
    @FXML private void handleCreateAuction()     {}
    @FXML private void handleRefreshAnalytics()  {}
    @FXML private void handleSavePlatform()      {}
    @FXML private void handleChangeAdminPw()     {}

    // ── Helpers ──

    private final String NORMAL_STYLE = "-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14;";
    private final String ACTIVE_STYLE = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14;";

    // ── Hàm điều hướng Tab  ──
    private void switchTab(Tab tab, String title, Button activeBtn) {
        if (mainTabPane != null && tab != null) {
            mainTabPane.getSelectionModel().select(tab);
        }
        if (lblPageTitle != null && title != null) {
            lblPageTitle.setText(title);
        }

        highlightButton(activeBtn);
    }

    // ── Hàm đổi màu nút ──
    private void highlightButton(Button active) {
        if (sidebarButtons == null) return;

        for (Button b : sidebarButtons) {
            if (b != null) {
                b.setStyle(NORMAL_STYLE);
            }
        }

        if (active != null) {
            active.setStyle(ACTIVE_STYLE);
        }
    }
}

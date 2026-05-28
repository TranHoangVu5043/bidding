
package Client.controller.admin;

import Client.model.Notification;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.NotificationApi;
import Client.networking.endpoints.UserApi;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminDashboardController {

    // ── Sidebar buttons ──
    @FXML private Button btnHome;
    @FXML private Button btnUsers;
    @FXML private Button btnSellers;
    @FXML private Button btnInventory;
    @FXML private Button btnAuctions;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;

    // ── Labels ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblPageTitle;
    @FXML private Label welcomeLabel;
    @FXML private Label lblNotifBadge;

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
    @FXML private Tab tabAuctions;
    @FXML private Tab tabAnalytics;
    @FXML private Tab tabNotifications;
    @FXML private Tab tabSettings;

    // ── Notification tab ──
    @FXML private Button btnNotifTop;
    @FXML private VBox   personalNotifList;
    @FXML private Button btnNotifAll;
    @FXML private Button btnNotifUnread;
    @FXML private Button btnNotifAuction;
    @FXML private Button btnNotifOrder;

    // ── Settings tab ──
    @FXML private Button toggleAuctionNotif;
    @FXML private Button toggleEmailNotif;
    @FXML private Button btnDeleteAccount;

    private Button[] sidebarButtons;

    // ── Notification state ──
    private final NotificationApi     notifApi   = new NotificationApi();
    private final UserApi             userApi    = new UserApi();
    private volatile List<Notification> cachedNotifications = List.of();
    private Thread notifPollerThread;

    @FXML
    public void initialize() {
        sidebarButtons = new Button[]{
            btnHome, btnUsers, btnSellers, btnInventory,
            btnAuctions, btnAnalytics, btnSettings
        };

        lblTotalRevenue.setText("1,337,000,000 ₫");
        lblPageTitle.setText("Dashboard");
        setupChart();
        populateActivityFeed();
        highlightButton(btnHome);
        populateAdminInfo();
        startNotifPoller();
    }

    private void populateAdminInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        if (welcomeLabel != null) welcomeLabel.setText(user.getUsername() != null ? user.getUsername() : "Admin");
        applyToggleState(toggleAuctionNotif, user.isNotifAuction());
        applyToggleState(toggleEmailNotif,   user.isNotifEmail());
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
    @FXML private void handleAuctions()      { switchTab(tabAuctions,      "Đấu Giá",btnAuctions);              highlightButton(btnAuctions); }
    @FXML private void handleAnalytics()     { switchTab(tabAnalytics,     "Phân Tích",btnAnalytics);            highlightButton(btnAnalytics); }
    @FXML private void handleSettings()      {
        switchTab(tabSettings, "Cài Đặt", btnSettings);
        highlightButton(btnSettings);
        User user = SessionManager.getCurrentUser();
        if (user != null) {
            applyToggleState(toggleAuctionNotif, user.isNotifAuction());
            applyToggleState(toggleEmailNotif,   user.isNotifEmail());
        }
    }

    @FXML
    private void handleSignOut() {
        if (notifPollerThread != null) notifPollerThread.interrupt();
        SessionManager.clear();
        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
    }

    @FXML
    private void handleNotifications() {
        switchTab(tabNotifications, "Thông Báo", null);
        loadPersonalNotifications();
    }

    // ── Other FXML handlers ──
    @FXML private void handleAddProduct()        {}
    @FXML private void handleExportUsers()       {}{}
    @FXML private void handleApproveSeller()     {}
    @FXML private void handleCreateAuction()     {}
    @FXML private void handleRefreshAnalytics()  {}
    @FXML private void handleSavePlatform()      {}
    @FXML private void handleChangeAdminPw()     {}
    @FXML private void handleSendNotification()  {}

    // ══════════════════════════════════════════
    // Notification Logic
    // ══════════════════════════════════════════

    private void startNotifPoller() {
        notifPollerThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    ApiResponse<List<Notification>> resp = notifApi.getNotifications();
                    if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                        List<Notification> fresh = resp.getData();
                        long unread = fresh.stream().filter(n -> !n.isRead()).count();
                        boolean countChanged = fresh.size() != cachedNotifications.size();
                        Platform.runLater(() -> {
                            if (lblNotifBadge != null)
                                lblNotifBadge.setText(unread > 0 ? String.valueOf(unread) : "0");
                            if (countChanged) {
                                cachedNotifications = fresh;
                                Tab sel = mainTabPane != null
                                        ? mainTabPane.getSelectionModel().getSelectedItem() : null;
                                if (sel == tabNotifications) renderPersonalNotifications(cachedNotifications);
                            }
                        });
                    }
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ignored) {}
            }
        });
        notifPollerThread.setDaemon(true);
        notifPollerThread.start();
    }

    private void loadPersonalNotifications() {
        if (personalNotifList == null) return;
        new Thread(() -> {
            ApiResponse<List<Notification>> resp = notifApi.getNotifications();
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    cachedNotifications = resp.getData();
                    long unread = cachedNotifications.stream().filter(n -> !n.isRead()).count();
                    if (lblNotifBadge != null) lblNotifBadge.setText(unread > 0 ? String.valueOf(unread) : "0");
                } else {
                    cachedNotifications = List.of();
                }
                setActiveNotifFilter(btnNotifAll);
                renderPersonalNotifications(cachedNotifications);
            });
        }).start();
    }

    private void renderPersonalNotifications(List<Notification> list) {
        if (personalNotifList == null) return;
        personalNotifList.getChildren().clear();
        if (list == null || list.isEmpty()) {
            Label empty = new Label("Không có thông báo cá nhân nào.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            personalNotifList.getChildren().add(empty);
            return;
        }
        for (Notification n : list) personalNotifList.getChildren().add(buildNotifRow(n));
    }

    private HBox buildNotifRow(Notification n) {
        Label icon = new Label(n.getMessage() != null && n.getMessage().contains("hủy") ? "🚫" : "🔔");
        icon.setStyle("-fx-font-size: 20;");

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.setMaxWidth(500);
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: " + (n.isRead() ? "#6B7280" : "#1e293b") + ";");
        HBox.setHgrow(msg, Priority.ALWAYS);

        Label time = new Label(formatNotifTime(n.getCreatedAt()));
        time.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF;");

        HBox row = new HBox(12, icon, msg, time);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        String bg     = n.isRead() ? "white"   : "#EFF6FF";
        String border = n.isRead() ? "#E5E7EB" : "#BFDBFE";
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: 1;");
        return row;
    }

    private String formatNotifTime(String isoTime) {
        if (isoTime == null) return "";
        try {
            java.time.LocalDateTime dt   = java.time.LocalDateTime.parse(isoTime);
            java.time.Duration      diff = java.time.Duration.between(dt, java.time.LocalDateTime.now());
            if (diff.toMinutes() < 1)  return "Vừa xong";
            if (diff.toMinutes() < 60) return diff.toMinutes() + " phút trước";
            if (diff.toHours()   < 24) return diff.toHours()   + " giờ trước";
            return diff.toDays() + " ngày trước";
        } catch (Exception e) { return isoTime; }
    }

    @FXML private void handleMarkAllRead() {
        new Thread(() -> {
            notifApi.markAllRead();
            Platform.runLater(this::loadPersonalNotifications);
        }).start();
    }

    @FXML private void filterNotifAll() {
        setActiveNotifFilter(btnNotifAll);
        renderPersonalNotifications(cachedNotifications);
    }

    @FXML private void filterNotifUnread() {
        setActiveNotifFilter(btnNotifUnread);
        renderPersonalNotifications(cachedNotifications.stream().filter(n -> !n.isRead()).toList());
    }

    @FXML private void filterNotifAuction() {
        setActiveNotifFilter(btnNotifAuction);
        renderPersonalNotifications(cachedNotifications.stream()
                .filter(n -> isAuctionNotif(n.getMessage())).toList());
    }

    @FXML private void filterNotifOrder() {
        setActiveNotifFilter(btnNotifOrder);
        renderPersonalNotifications(cachedNotifications.stream()
                .filter(n -> !isAuctionNotif(n.getMessage())).toList());
    }

    private boolean isAuctionNotif(String msg) {
        if (msg == null) return false;
        String lower = msg.toLowerCase();
        return lower.contains("đấu giá") || lower.contains("auction")
                || lower.contains("vượt giá") || lower.contains("thắng")
                || lower.contains("giá hiện tại") || msg.contains("🎉") || msg.contains("📢");
    }

    private void setActiveNotifFilter(Button active) {
        Button[] filters = { btnNotifAll, btnNotifUnread, btnNotifAuction, btnNotifOrder };
        for (Button btn : filters) {
            if (btn == null) continue;
            if (btn == active) {
                btn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; "
                        + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; "
                        + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
            }
        }
    }

    // ══════════════════════════════════════════
    // Settings Logic
    // ══════════════════════════════════════════

    private void applyToggleState(Button btn, boolean on) {
        if (btn == null) return;
        btn.setText(on ? "Bật" : "Tắt");
        btn.setStyle("-fx-background-color: " + (on ? "#16A34A" : "#9CA3AF") + "; -fx-text-fill: white; "
                + "-fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;");
    }

    @FXML
    private void handleToggleNotification(ActionEvent event) {
        Button btn = (Button) event.getSource();
        boolean turningOn = btn.getText().equals("Tắt");
        applyToggleState(btn, turningOn);

        if (btn != toggleAuctionNotif && btn != toggleEmailNotif) return;

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        boolean notifAuction = toggleAuctionNotif != null && toggleAuctionNotif.getText().equals("Bật");
        boolean notifEmail   = toggleEmailNotif   != null && toggleEmailNotif.getText().equals("Bật");

        user.setNotifAuction(notifAuction);
        user.setNotifEmail(notifEmail);

        new Thread(() -> {
            ApiResponse<Void> resp = userApi.updatePreferences(notifAuction, notifEmail);
            if (resp == null || resp.getStatus() != 200) {
                Platform.runLater(() -> {
                    applyToggleState(btn, !turningOn);
                    if (btn == toggleAuctionNotif) user.setNotifAuction(!turningOn);
                    else                           user.setNotifEmail(!turningOn);
                    SceneUtil.showAlert("Lỗi", "Không thể lưu tùy chọn thông báo.");
                });
            }
        }).start();
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa tài khoản");
        confirm.setHeaderText("Bạn có chắc muốn xóa tài khoản?");
        confirm.setContentText("Hành động này không thể hoàn tác. Toàn bộ dữ liệu sẽ bị xóa vĩnh viễn.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;
            if (btnDeleteAccount != null) btnDeleteAccount.setDisable(true);
            new Thread(() -> {
                ApiResponse<Void> resp = userApi.deleteAccount();
                Platform.runLater(() -> {
                    if (btnDeleteAccount != null) btnDeleteAccount.setDisable(false);
                    if (resp != null && resp.getStatus() == 200) {
                        if (notifPollerThread != null) notifPollerThread.interrupt();
                        SessionManager.clear();
                        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
                    } else {
                        String msg = resp != null ? resp.getMessage() : "Mất kết nối tới Server";
                        SceneUtil.showAlert("Lỗi", "Không thể xóa tài khoản: " + msg);
                    }
                });
            }).start();
        });
    }

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

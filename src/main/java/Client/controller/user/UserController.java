package Client.controller.user;

import Client.model.Notification;
import Client.model.auction.Auction;
import Client.model.auction.Bid;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.websocket.AuctionWebSocketClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.BidApi;
import Client.networking.endpoints.NotificationApi;
import Client.networking.endpoints.UserApi;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserController {

    // ══════════════════════════════════════════
    // FXML — Top Navbar
    // ══════════════════════════════════════════
    @FXML private TextField txtSearch;
    @FXML private Button    btnNotifTop;
    @FXML private Label     lblNotifCount;
    @FXML private Label     lblUsername;
    @FXML private Circle    avatarCircle;

    // ══════════════════════════════════════════
    // FXML — Sidebar buttons
    // ══════════════════════════════════════════
    @FXML private Button btnDashBoard;
    @FXML private Button btnAuction;
    @FXML private Button btnNotification;
    @FXML private Button btnProfile;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;
    @FXML private Label  lblSidebarName;

    // ══════════════════════════════════════════
    // FXML — TabPane
    // ══════════════════════════════════════════
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabNotification;
    @FXML private Tab tabProfile;
    @FXML private Tab tabSettings;

    // ══════════════════════════════════════════
    // FXML — Tab Dashboard (Auction Floor)
    // ══════════════════════════════════════════
    @FXML private Label              lblActiveBids;
    @FXML private Label              lblWonAuctions;
    @FXML private ComboBox<String>   cmbFilter;
    @FXML private FlowPane           auctionFlowPane;
    @FXML private Button             btnRefreshAuctions;

    // ══════════════════════════════════════════
    // FXML — Tab Notification
    // ══════════════════════════════════════════
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnNotifAll;
    @FXML private Button btnNotifUnread;
    @FXML private Button btnNotifAuction;
    @FXML private Button btnNotifOrder;
    @FXML private VBox   notificationList;

    // ══════════════════════════════════════════
    // FXML — Tab Profile
    // ══════════════════════════════════════════
    @FXML private Label         lblProfileName;
    @FXML private Label         lblProfileEmail;
    @FXML private Button        btnChangeAvatar;
    @FXML private TextField     txtFullName;
    @FXML private TextField     txtPhone;
    @FXML private TextField     txtEmail;
    @FXML private TextField     txtAddress;
    @FXML private Button        btnSaveProfile;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button        btnChangePassword;

    // ══════════════════════════════════════════
    // FXML — Tab Settings
    // ══════════════════════════════════════════
    @FXML private Button           toggleAuctionNotif;
    @FXML private Button           toggleOrderNotif;
    @FXML private Button           toggleEmailNotif;
    @FXML private Button           toggle2FA;
    @FXML private ComboBox<String> cmbLanguage;
    @FXML private ComboBox<String> cmbCurrency;
    @FXML private Button           btnDeleteAccount;

    // ══════════════════════════════════════════
    // APIs & Data
    // ══════════════════════════════════════════
    private final UserApi         userApi    = new UserApi();
    private final AuctionApi      auctionApi = new AuctionApi();
    private final BidApi          bidApi     = new BidApi();
    private final NotificationApi notifApi   = new NotificationApi();

    private final ObservableList<Auction> liveAuctions   = FXCollections.observableArrayList();
    /** auctionId → the price Label inside that card, for targeted live updates */
    private final Map<Integer, Label>     livePriceLabels = new ConcurrentHashMap<>();
    /** Single persistent WebSocket connection for the auction floor */
    private AuctionWebSocketClient wsClient;

    // ══════════════════════════════════════════
    // Initialize
    // ══════════════════════════════════════════
    @FXML
    public void initialize() {
        setupComboBoxes();
        populateUserInfo();
        loadAuctions();
        loadNotifications();

        // Re-filter cards whenever the ComboBox value changes
        cmbFilter.valueProperty().addListener((obs, old, val) -> renderAuctionCards(liveAuctions));
    }

    /** Reads the User saved by LoginController and fills every label/field that shows user data. */
    private void populateUserInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String displayName = user.getUsername() != null ? user.getUsername() : "—";
        String email       = user.getEmail()    != null ? user.getEmail()    : "—";

        if (lblUsername    != null) lblUsername.setText(displayName);
        if (lblSidebarName != null) lblSidebarName.setText(displayName);
        if (lblProfileName != null) lblProfileName.setText(displayName);
        if (lblProfileEmail != null) lblProfileEmail.setText(email);
        if (txtFullName    != null) txtFullName.setText(displayName);
        if (txtEmail       != null) txtEmail.setText(email);
    }

    // ══════════════════════════════════════════
    // Setup helpers
    // ══════════════════════════════════════════
    private void setupComboBoxes() {
        cmbFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang chạy", "Sắp diễn ra", "Đã kết thúc"));
        cmbLanguage.setItems(FXCollections.observableArrayList("Tiếng Việt", "English"));
        cmbCurrency.setItems(FXCollections.observableArrayList("VNĐ", "USD"));
    }

    // ══════════════════════════════════════════
    // Sidebar Navigation
    // ══════════════════════════════════════════
    @FXML private void handleDashBoard()    { switchTab(tabDashboard,    "Dashboard"); }
    @FXML private void handleAuction()      { switchTab(tabDashboard,    "Sàn Đấu Giá"); loadAuctions(); }
    @FXML private void handleNotification() { switchTab(tabNotification, "Thông Báo"); loadNotifications(); }
    @FXML private void handleProfile()      { switchTab(tabProfile,      "Hồ Sơ Cá Nhân"); }
    @FXML private void handleSettings()     { switchTab(tabSettings,     "Cài Đặt"); }

    @FXML
    private void handleSignOut() {
        if (wsClient != null) wsClient.closeConnection();
        SessionManager.clear();
        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
    }

    // ══════════════════════════════════════════
    // Auction Floor — Load & Render
    // ══════════════════════════════════════════

    /** Fetches all auctions on a background thread, then rebuilds the card grid. */
    @FXML
    private void handleRefreshAuctions() { loadAuctions(); }

    private void loadAuctions() {
        if (auctionFlowPane == null) return;

        auctionFlowPane.getChildren().clear();
        Label loading = new Label("⏳ Đang tải danh sách phiên đấu giá...");
        loading.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
        auctionFlowPane.getChildren().add(loading);

        if (btnRefreshAuctions != null) btnRefreshAuctions.setDisable(true);

        new Thread(() -> {
            ApiResponse<List<Auction>> response = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (btnRefreshAuctions != null) btnRefreshAuctions.setDisable(false);

                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    liveAuctions.setAll(response.getData());
                    livePriceLabels.clear();
                    renderAuctionCards(liveAuctions);
                    updateAuctionStats();
                    connectWebSocket();   // subscribe to all active auctions
                } else {
                    auctionFlowPane.getChildren().clear();
                    String msg = response != null ? response.getMessage() : "Mất kết nối tới Server";
                    Label err = new Label("❌ Không thể tải phiên đấu giá: " + msg);
                    err.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13;");
                    auctionFlowPane.getChildren().add(err);
                }
            });
        }).start();
    }

    /** Opens (or reuses) the WebSocket connection and subscribes to every ACTIVE auction. */
    private void connectWebSocket() {
        // Close stale connection if any
        if (wsClient != null && !wsClient.isClosed()) wsClient.closeConnection();

        wsClient = new AuctionWebSocketClient((auctionId, newPrice) -> {
            // Update the in-memory auction object
            liveAuctions.stream()
                    .filter(a -> a.getId() == auctionId)
                    .findFirst()
                    .ifPresent(a -> a.setCurrentPrice(newPrice));

            // Update only the price label for that card — no full re-render
            Label lbl = livePriceLabels.get(auctionId);
            if (lbl != null) {
                lbl.setText("Giá hiện tại: " + String.format("%,.0f ₫", newPrice));
                lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #16A34A;");
            }
        });

        // connect() is non-blocking — safe to call on FX thread
        wsClient.connect();

        // Subscribe to every active auction once the socket opens
        // We do this on a background thread with a short wait for the handshake
        List<Integer> activeIds = liveAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .map(Auction::getId)
                .toList();

        new Thread(() -> {
            // Wait up to 2 s for the connection to open
            for (int i = 0; i < 20; i++) {
                if (wsClient.isOpen()) break;
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            activeIds.forEach(wsClient::subscribe);
            System.out.println("[WS Client] Subscribed to " + activeIds.size() + " active auction(s): " + activeIds);
        }).start();
    }

    /** Applies the current filter value and rebuilds the FlowPane cards. Must run on FX thread. */
    private void renderAuctionCards(List<Auction> auctions) {
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();

        String filter = cmbFilter.getValue();
        List<Auction> visible = auctions.stream().filter(a -> {
            String s = a.getStatus() != null ? a.getStatus().toUpperCase() : "";
            // Always hide CANCELLED in "Tất cả" view
            if ("CANCELLED".equalsIgnoreCase(s) && (filter == null || filter.equals("Tất cả"))) return false;
            if (filter == null || filter.equals("Tất cả")) return true;
            return switch (filter) {
                case "Đang chạy"    -> s.equals("ACTIVE");
                case "Sắp diễn ra"  -> s.equals("UPCOMING");
                case "Đã kết thúc"  -> s.equals("FINISHED") || s.equals("CANCELLED");
                default -> true;
            };
        }).collect(Collectors.toList());

        if (visible.isEmpty()) {
            Label empty = new Label("Không có phiên đấu giá nào phù hợp.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            auctionFlowPane.getChildren().add(empty);
            return;
        }

        for (Auction auction : visible) {
            auctionFlowPane.getChildren().add(buildAuctionCard(auction));
        }
    }

    /** Builds one auction card VBox programmatically to match the FXML design style. */
    private VBox buildAuctionCard(Auction auction) {
        String status = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";

        // ── Status badge ──
        String badgeColor = switch (status) {
            case "ACTIVE"    -> "#16A34A";
            case "UPCOMING"  -> "#0066CC";
            case "FINISHED"  -> "#6B7280";
            default          -> "#D32F2F";  // CANCELLED
        };
        String statusText = switch (status) {
            case "ACTIVE"    -> "● Đang diễn ra";
            case "UPCOMING"  -> "● Sắp diễn ra";
            case "FINISHED"  -> "✓ Đã kết thúc";
            default          -> "✕ Đã hủy";
        };
        Label lblStatus = new Label(statusText);
        lblStatus.setStyle("-fx-text-fill: " + badgeColor + "; -fx-font-size: 11; -fx-font-weight: bold;");

        // ── Image placeholder (click → detail popup) ──
        Region imgBg = new Region();
        imgBg.setPrefSize(186, 120);
        imgBg.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 8;");
        Label imgLabel = new Label("🏷  Mặt hàng #" + auction.getItemId());
        imgLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        Label hintLabel = new Label("Nhấn để xem chi tiết");
        hintLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF; -fx-padding: 4 0 0 0;");
        VBox imgContent = new VBox(4, imgLabel, hintLabel);
        imgContent.setAlignment(Pos.CENTER);
        StackPane imgPane = new StackPane(imgBg, imgContent);
        imgPane.setStyle("-fx-cursor: hand;");
        imgPane.setOnMouseClicked(e -> showAuctionDetail(auction));

        // ── Title ──
        Label lblTitle = new Label("Phiên đấu giá #" + auction.getId());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        lblTitle.setWrapText(true);

        // ── Seller name ──
        Label lblSeller = new Label("🏪 " + (auction.getSellerName() != null ? auction.getSellerName() : "—"));
        lblSeller.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        // ── Prices ──
        Label lblStartPrice = new Label(
                "Khởi điểm: " + String.format("%,.0f ₫", auction.getStartingPrice()));
        lblStartPrice.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");

        Label lblCurrentPrice = new Label(
                "Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        lblCurrentPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #D32F2F;");
        // Register for live WebSocket updates
        if ("ACTIVE".equals(status)) livePriceLabels.put(auction.getId(), lblCurrentPrice);

        // ── Time remaining ──
        Label lblTime = new Label("🕒 " + formatTimeRemaining(auction.getEndTime()));
        lblTime.setStyle("-fx-font-size: 11; -fx-text-fill: #0066CC;");

        // ── Place bid button (active only for ACTIVE auctions) ──
        boolean canBid = "ACTIVE".equals(status);
        Button btnBid = new Button("Đặt giá ngay");
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setDisable(!canBid);
        btnBid.setStyle("-fx-background-color: " + (canBid ? "#0066CC" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 8;");
        if (canBid) {
            btnBid.setOnAction(e -> handlePlaceBid(auction));
        }

        // ── Bid history button ──
        Button btnHistory = new Button("Lịch sử đấu giá");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 6;");
        btnHistory.setOnAction(e -> showBidHistory(auction));

        // ── Assemble card ──
        VBox card = new VBox(8,
                lblStatus, imgPane, lblTitle, lblSeller,
                lblStartPrice, lblCurrentPrice, lblTime,
                btnBid, btnHistory);
        card.setPrefWidth(210);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.09), 10, 0, 0, 3); " +
                "-fx-padding: 12;");
        return card;
    }

    /** Formats the end-time ISO string as a human-readable remaining time. */
    private String formatTimeRemaining(String endTimeStr) {
        if (endTimeStr == null) return "Không xác định";
        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            LocalDateTime now = LocalDateTime.now();
            if (!now.isBefore(endTime)) return "Đã kết thúc";
            Duration diff = Duration.between(now, endTime);
            long days    = diff.toDays();
            long hours   = diff.toHoursPart();
            long minutes = diff.toMinutesPart();
            if (days > 0) return String.format("Còn %d ngày %02d:%02d", days, hours, minutes);
            return String.format("Còn %02d giờ %02d phút", hours, minutes);
        } catch (Exception e) {
            return endTimeStr;  // fallback: show raw string
        }
    }

    /** Updates the dashboard stat cards with live counts. */
    private void updateAuctionStats() {
        long active = liveAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .count();
        long finished = liveAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                .count();
        if (lblActiveBids  != null) lblActiveBids.setText(active + " phiên");
        if (lblWonAuctions != null) lblWonAuctions.setText(finished + " phiên");
    }

    // ══════════════════════════════════════════
    // Bidding
    // ══════════════════════════════════════════

    /** Shows a TextInputDialog for the bid amount, validates, then posts on a background thread. */
    private void handlePlaceBid(Auction auction) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Đặt giá – Phiên #" + auction.getId());
        dialog.setHeaderText("Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        dialog.setContentText("Nhập số tiền muốn đặt (₫):");
        dialog.showAndWait().ifPresent(input -> {
            try {
                // Accept numbers with commas/dots as thousand separators
                double amount = Double.parseDouble(input.replace(",", "").replace(".", "").trim());
                if (amount <= auction.getCurrentPrice()) {
                    showAlert(Alert.AlertType.WARNING, "Giá không hợp lệ",
                            String.format("Số tiền phải lớn hơn giá hiện tại (%,.0f ₫).",
                                    auction.getCurrentPrice()));
                    return;
                }

                new Thread(() -> {
                    ApiResponse<Void> resp = bidApi.placeBid(auction.getId(), amount);
                    Platform.runLater(() -> {
                        if (resp != null && resp.getStatus() == 201) {
                            showAlert(Alert.AlertType.INFORMATION, "Đặt giá thành công",
                                    String.format("Bạn đã đặt giá %,.0f ₫ thành công!", amount));
                            loadAuctions(); // Refresh to reflect new current price
                        } else {
                            String msg = resp != null ? resp.getMessage() : "Mất kết nối tới Server";
                            showAlert(Alert.AlertType.ERROR, "Đặt giá thất bại", msg);
                        }
                    });
                }).start();

            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu",
                        "Vui lòng nhập một số tiền hợp lệ (ví dụ: 1500000).");
            }
        });
    }

    /** Fetches bid history for an auction and shows it in a dialog with a TableView. */
    private void showBidHistory(Auction auction) {
        // Build the table first (shown immediately; data fills in asynchronously)
        TableView<Bid> table = new TableView<>();
        table.setPrefSize(480, 280);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("⏳ Đang tải..."));

        TableColumn<Bid, Integer> colUser = new TableColumn<>("Người đặt (ID)");
        colUser.setCellValueFactory(new PropertyValueFactory<>("userId"));

        TableColumn<Bid, Double> colAmount = new TableColumn<>("Số tiền");
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colAmount.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("%,.0f ₫", val));
            }
        });

        TableColumn<Bid, String> colTime = new TableColumn<>("Thời gian");
        colTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        table.getColumns().addAll(colUser, colAmount, colTime);

        VBox content = new VBox(10, table);
        content.setPadding(new Insets(10));

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lịch sử đấu giá – Phiên #" + auction.getId());
        dialog.setHeaderText("Phiên #" + auction.getId()
                + "  |  Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        // Fetch in background; update table when done
        new Thread(() -> {
            ApiResponse<List<Bid>> resp = bidApi.getBidHistory(auction.getId());
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    table.setItems(FXCollections.observableArrayList(resp.getData()));
                    if (resp.getData().isEmpty()) {
                        table.setPlaceholder(new Label("Chưa có lượt đặt giá nào."));
                    }
                } else {
                    String msg = resp != null ? resp.getMessage() : "Mất kết nối";
                    table.setPlaceholder(new Label("Không thể tải lịch sử: " + msg));
                }
            });
        }).start();

        dialog.showAndWait();
    }

    /** Shows a styled detail popup for a clicked auction card. */
    private void showAuctionDetail(Auction auction) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Chi tiết phiên đấu giá #" + auction.getId());
        popup.setResizable(false);

        String status = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";

        // ── Header colours ──
        String headerBg = switch (status) {
            case "ACTIVE"   -> "linear-gradient(to bottom, #dbeafe, #93c5fd)";
            case "UPCOMING" -> "linear-gradient(to bottom, #e0e7ff, #a5b4fc)";
            case "FINISHED" -> "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
            default         -> "linear-gradient(to bottom, #fee2e2, #fca5a5)";
        };
        String badgeColor = switch (status) {
            case "ACTIVE"   -> "#16A34A";
            case "UPCOMING" -> "#0066CC";
            case "FINISHED" -> "#6B7280";
            default         -> "#D32F2F";
        };
        String statusText = switch (status) {
            case "ACTIVE"   -> "● Đang diễn ra";
            case "UPCOMING" -> "● Sắp diễn ra";
            case "FINISHED" -> "✓ Đã kết thúc";
            default         -> "✕ Đã hủy";
        };

        Label iconLabel = new Label("🏷");
        iconLabel.setStyle("-fx-font-size: 52;");

        Label titleLabel = new Label("Phiên đấu giá #" + auction.getId());
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label statusBadge = new Label(statusText);
        statusBadge.setStyle("-fx-text-fill: " + badgeColor + "; -fx-font-size: 12; -fx-font-weight: bold;");

        VBox header = new VBox(8, iconLabel, titleLabel, statusBadge);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 24, 16, 24));
        header.setStyle("-fx-background-color: " + headerBg + ";");

        // ── Detail rows ──
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 4, 28));

        addAuctionDetailRow(grid, 0, "📦 Mã sản phẩm",  "#" + auction.getItemId());
        addAuctionDetailRow(grid, 1, "💵 Giá khởi điểm", String.format("%,.0f ₫", auction.getStartingPrice()));

        addAuctionDetailRow(grid, 2, "🔥 Giá hiện tại",
                String.format("%,.0f ₫", auction.getCurrentPrice()));

        addAuctionDetailRow(grid, 3, "🕐 Bắt đầu",  formatDisplayTime(auction.getStartTime()));
        addAuctionDetailRow(grid, 4, "🕔 Kết thúc", formatDisplayTime(auction.getEndTime()));
        addAuctionDetailRow(grid, 5, "⏳ Còn lại",  formatTimeRemaining(auction.getEndTime()));

        // ── Divider ──
        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 24, 0, 24));

        // ── Action buttons ──
        boolean canBid = "ACTIVE".equals(status);

        Button btnBid = new Button("Đặt giá ngay  💸");
        btnBid.setDisable(!canBid);
        btnBid.setMaxWidth(Double.MAX_VALUE);
        btnBid.setStyle("-fx-background-color: " + (canBid ? "#0066CC" : "#9CA3AF") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: " + (canBid ? "hand" : "default") + ";");
        if (canBid) {
            btnBid.setOnAction(e -> { popup.close(); handlePlaceBid(auction); });
        }

        Button btnHistory = new Button("Lịch sử đặt giá  📋");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
        btnHistory.setOnAction(e -> { popup.close(); showBidHistory(auction); });

        Button btnClose = new Button("← Quay lại");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());

        VBox btnBox = new VBox(8, btnBid, btnHistory, btnClose);
        btnBox.setPadding(new Insets(14, 28, 24, 28));

        VBox root = new VBox(header, grid, sep, btnBox);
        root.setStyle("-fx-background-color: #f8fafc;");

        popup.setScene(new Scene(root, 420, 520));
        popup.showAndWait();
    }

    private void addAuctionDetailRow(GridPane grid, int row, String labelText, String value) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 12;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private String formatDisplayTime(String timeStr) {
        if (timeStr == null) return "—";
        // ISO: "2026-06-01T10:00:00" → "01/06/2026 10:00"
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(timeStr);
            return String.format("%02d/%02d/%d %02d:%02d",
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                    dt.getHour(), dt.getMinute());
        } catch (Exception e) {
            return timeStr;
        }
    }

    // ══════════════════════════════════════════
    // Search
    // ══════════════════════════════════════════
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            renderAuctionCards(liveAuctions);
            return;
        }
        List<Auction> filtered = liveAuctions.stream()
                .filter(a -> String.valueOf(a.getId()).contains(keyword)
                        || String.valueOf(a.getItemId()).contains(keyword))
                .collect(Collectors.toList());
        switchTab(tabDashboard, "Dashboard");
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("Không tìm thấy phiên đấu giá phù hợp.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            auctionFlowPane.getChildren().add(empty);
            return;
        }
        for (Auction auction : filtered) {
            auctionFlowPane.getChildren().add(buildAuctionCard(auction));
        }
    }

    // ══════════════════════════════════════════
    // Notification
    // ══════════════════════════════════════════
    @FXML private void handleMarkAllRead() {
        new Thread(() -> {
            notifApi.markAllRead();
            Platform.runLater(this::loadNotifications);
        }).start();
    }
    @FXML private void filterNotifAll()      { /* TODO */ }
    @FXML private void filterNotifUnread()   { /* TODO */ }
    @FXML private void filterNotifAuction()  { /* TODO */ }
    @FXML private void filterNotifOrder()    { /* TODO */ }

    private void loadNotifications() {
        if (notificationList == null) return;
        new Thread(() -> {
            ApiResponse<List<Notification>> resp = notifApi.getNotifications();
            Platform.runLater(() -> {
                notificationList.getChildren().clear();
                if (resp == null || resp.getStatus() != 200 || resp.getData() == null || resp.getData().isEmpty()) {
                    Label empty = new Label("Không có thông báo nào.");
                    empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
                    notificationList.getChildren().add(empty);
                    if (lblNotifCount != null) lblNotifCount.setText("0");
                    return;
                }
                long unread = resp.getData().stream().filter(n -> !n.isRead()).count();
                if (lblNotifCount != null) lblNotifCount.setText(unread > 0 ? String.valueOf(unread) : "0");
                for (Notification n : resp.getData()) {
                    notificationList.getChildren().add(buildNotifRow(n));
                }
            });
        }).start();
    }

    private HBox buildNotifRow(Notification n) {
        Label icon = new Label(n.getMessage().contains("hủy") ? "🚫" : "🔔");
        icon.setStyle("-fx-font-size: 20;");

        Label msg = new Label(n.getMessage());
        msg.setWrapText(true);
        msg.setMaxWidth(500);
        msg.setStyle("-fx-font-size: 12; -fx-text-fill: " + (n.isRead() ? "#6B7280" : "#1e293b") + ";");
        HBox.setHgrow(msg, javafx.scene.layout.Priority.ALWAYS);

        Label time = new Label(formatNotifTime(n.getCreatedAt()));
        time.setStyle("-fx-font-size: 10; -fx-text-fill: #9CA3AF;");

        HBox row = new HBox(12, icon, msg, time);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        String bg = n.isRead() ? "white" : "#EFF6FF";
        String border = n.isRead() ? "#E5E7EB" : "#BFDBFE";
        row.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: 1;");
        return row;
    }

    private String formatNotifTime(String isoTime) {
        if (isoTime == null) return "";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(isoTime);
            java.time.Duration diff = java.time.Duration.between(dt, java.time.LocalDateTime.now());
            if (diff.toMinutes() < 1) return "Vừa xong";
            if (diff.toMinutes() < 60) return diff.toMinutes() + " phút trước";
            if (diff.toHours() < 24) return diff.toHours() + " giờ trước";
            return diff.toDays() + " ngày trước";
        } catch (Exception e) { return isoTime; }
    }

    // ══════════════════════════════════════════
    // Profile
    // ══════════════════════════════════════════
    @FXML
    private void handleSaveProfile() {
        String name  = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng điền đầy đủ họ tên, số điện thoại và email.");
            return;
        }
        if (lblUsername    != null) lblUsername.setText(name);
        if (lblSidebarName != null) lblSidebarName.setText(name);
        if (lblProfileName != null) lblProfileName.setText(name);
        if (lblProfileEmail != null) lblProfileEmail.setText(email);

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            user.setUsername(name);
            user.setEmail(email);
        }
        // TODO: send update to server
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thông tin cá nhân đã được cập nhật.");
    }

    @FXML
    private void handleChangePassword() {
        String oldPw     = txtOldPassword.getText();
        String newPw     = txtNewPassword.getText();
        String confirmPw = txtConfirmPassword.getText();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng điền đầy đủ các ô mật khẩu.");
            return;
        }
        if (!newPw.equals(confirmPw)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới và xác nhận không khớp.");
            return;
        }
        if (newPw.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu quá ngắn",
                    "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        btnChangePassword.setDisable(true);
        btnChangePassword.setText("Đang xử lý...");

        new Thread(() -> {
            ApiResponse<Void> resp = userApi.changePassword(oldPw, newPw);
            Platform.runLater(() -> {
                btnChangePassword.setDisable(false);
                btnChangePassword.setText("Đổi Mật Khẩu");
                if (resp != null && resp.getStatus() == 200) {
                    txtOldPassword.clear();
                    txtNewPassword.clear();
                    txtConfirmPassword.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Mật khẩu đã được thay đổi.");
                } else {
                    String msg = resp != null ? resp.getMessage() : "Mất kết nối tới Server";
                    showAlert(Alert.AlertType.ERROR, "Đổi mật khẩu thất bại", msg);
                }
            });
        }).start();
    }

    @FXML private void handleChangeAvatar() { /* TODO: open image picker dialog */ }

    // ══════════════════════════════════════════
    // Settings
    // ══════════════════════════════════════════
    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa tài khoản? Hành động này không thể hoàn tác.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: send delete request to server
                SessionManager.clear();
                SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
            }
        });
    }

    // ══════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════
    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) {
            mainTabPane.getSelectionModel().select(tab);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

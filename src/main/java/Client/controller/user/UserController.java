package Client.controller.user;

import Client.model.auction.Auction;
import Client.model.auction.Bid;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.BidApi;
import Client.networking.endpoints.UserApi;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class UserController {
    // ══════════════════════════════════════════
    // FXML — Top Navbar
    // ══════════════════════════════════════════
    @FXML private TextField txtSearch;
    @FXML private Button    btnCartTop;
    @FXML private Label     lblCartCount;
    @FXML private Button    btnNotifTop;
    @FXML private Label     lblNotifCount;
    @FXML private Label     lblUsername;
    @FXML private Circle    avatarCircle;

    // ══════════════════════════════════════════
    // FXML — Sidebar buttons
    // ══════════════════════════════════════════
    @FXML private Button btnDashBoard;
    @FXML private Button btnShop;
    @FXML private Button btnAuction;
    @FXML private Button btnCart;
    @FXML private Button btnOrderHistory;
    @FXML private Button btnNotification;
    @FXML private Button btnProfile;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;
    @FXML private Label  lblSidebarName;
    @FXML private Button btnFollow ;
    @FXML private Button btnHistory;

    // ══════════════════════════════════════════
    // FXML — TabPane
    // ══════════════════════════════════════════
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabShop;
    @FXML private Tab tabCart;
    @FXML private Tab tabOrderHistory;
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
    // FXML — Tab Shop
    // ══════════════════════════════════════════
    @FXML private TextField        txtShopSearch;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbSort;
    @FXML private FlowPane         shopFlowPane;



    // ══════════════════════════════════════════
    // FXML — Tab Cart
    // ══════════════════════════════════════════
    @FXML private TableView<CartItem>            tableCart;
    @FXML private TableColumn<CartItem, String>  colCartImage;
    @FXML private TableColumn<CartItem, String>  colCartName;
    @FXML private TableColumn<CartItem, Double>  colCartPrice;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double>  colCartTotal;
    @FXML private TableColumn<CartItem, String>  colCartAction;
    @FXML private Label  lblSubtotal;
    @FXML private Label  lblShipping;
    @FXML private Label  lblCartTotal;
    @FXML private Button btnCheckout;
    @FXML private Button btnContinueShopping;

    // ══════════════════════════════════════════
    // FXML — Tab Order History
    // ══════════════════════════════════════════
    @FXML private ComboBox<String>               cmbOrderStatus;
    @FXML private TextField                      txtOrderSearch;
    @FXML private TableView<OrderItem>           tableOrderHistory;
    @FXML private TableColumn<OrderItem, String> colOrderId;
    @FXML private TableColumn<OrderItem, String> colOrderDate;
    @FXML private TableColumn<OrderItem, String> colOrderItems;
    @FXML private TableColumn<OrderItem, Double> colOrderTotal;
    @FXML private TableColumn<OrderItem, String> colOrderStatus;
    @FXML private TableColumn<OrderItem, String> colOrderDetail;

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
    private final UserApi    userApi    = new UserApi();
    private final AuctionApi auctionApi = new AuctionApi();
    private final BidApi     bidApi     = new BidApi();

    private final ObservableList<Auction>  liveAuctions = FXCollections.observableArrayList();
    private final ObservableList<CartItem>  cartItems   = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItems  = FXCollections.observableArrayList();
    private int cartCount = 0;

    // ══════════════════════════════════════════
    // Initialize
    // ══════════════════════════════════════════
    @FXML
    public void initialize() {
        setupCartTable();
        setupOrderTable();
        setupComboBoxes();
        updateCartBadge();
        updateNotificationBadge();
        populateUserInfo();
        loadAuctions();

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
    private void setupCartTable() {
        colCartName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        tableCart.setItems(cartItems);
    }

    private void setupOrderTable() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOrderItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableOrderHistory.setItems(orderItems);
    }

    private void setupComboBoxes() {
        cmbFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang chạy", "Sắp diễn ra", "Đã kết thúc"));
        cmbCategory.setItems(FXCollections.observableArrayList(
                "Tất cả", "Điện tử", "Thời trang", "Đồ gia dụng"));
        cmbSort.setItems(FXCollections.observableArrayList(
                "Mới nhất", "Giá tăng dần", "Giá giảm dần"));
        cmbOrderStatus.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy"));
        cmbLanguage.setItems(FXCollections.observableArrayList("Tiếng Việt", "English"));
        cmbCurrency.setItems(FXCollections.observableArrayList("VNĐ", "USD"));
    }

    private void updateCartBadge() {
        if (lblCartCount != null) lblCartCount.setText(String.valueOf(cartCount));
    }

    private void updateNotificationBadge(){if (  lblNotifCount != null)    lblNotifCount.setText(String.valueOf(notificationList));}

    // ══════════════════════════════════════════
    // Sidebar Navigation
    // ══════════════════════════════════════════
    @FXML private void handleDashBoard()    { switchTab(tabDashboard,    "Dashboard"); }
    @FXML private void handleShop()         { switchTab(tabShop,         "Shop"); }
    @FXML private void handleAuction()      { switchTab(tabDashboard,    "Sàn Đấu Giá"); loadAuctions(); }
    @FXML private void handleCart()         { switchTab(tabCart,         "Giỏ Hàng"); }
    @FXML private void handleOrderHistory() { switchTab(tabOrderHistory, "Lịch Sử Đơn Hàng"); }
    @FXML private void handleNotification() { switchTab(tabNotification, "Thông Báo"); }
    @FXML private void handleProfile()      { switchTab(tabProfile,      "Hồ Sơ Cá Nhân"); }
    @FXML private void handleSettings()     { switchTab(tabSettings,     "Cài Đặt"); }

    @FXML
    private void handleSignOut() {
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

        // Show a spinner placeholder while loading
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
                    renderAuctionCards(liveAuctions);
                    updateAuctionStats();
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

    /** Applies the current filter value and rebuilds the FlowPane cards. Must run on FX thread. */
    private void renderAuctionCards(List<Auction> auctions) {
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();

        String filter = cmbFilter.getValue();
        List<Auction> visible = auctions.stream().filter(a -> {
            if (filter == null || filter.equals("Tất cả")) return true;
            String s = a.getStatus() != null ? a.getStatus().toUpperCase() : "";
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

        // ── Image placeholder ──
        Region imgBg = new Region();
        imgBg.setPrefSize(186, 120);
        imgBg.setStyle("-fx-background-color: #EFF6FF; -fx-background-radius: 8;");
        Label imgLabel = new Label("🏷  Mặt hàng #" + auction.getItemId());
        imgLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");
        StackPane imgPane = new StackPane(imgBg, imgLabel);

        // ── Title ──
        Label lblTitle = new Label("Phiên đấu giá #" + auction.getId());
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
        lblTitle.setWrapText(true);

        // ── Prices ──
        Label lblStartPrice = new Label(
                "Khởi điểm: " + String.format("%,.0f ₫", auction.getStartingPrice()));
        lblStartPrice.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");

        Label lblCurrentPrice = new Label(
                "Giá hiện tại: " + String.format("%,.0f ₫", auction.getCurrentPrice()));
        lblCurrentPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #D32F2F;");

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
                lblStatus, imgPane, lblTitle,
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

    //RegisterAuction
    @FXML
    private void handleRegisterAuction(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/AuctionDetailView.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Không tìm thấy file: /client/AuctionDetailView.fxml");
            e.printStackTrace();
        }
    }
    // AddCart

    @FXML
    private void handleAddCart(Auction auction) {
        if (auction != null) {
            if (!cartItems.contains(auction)) {
                cartItems.add(auction);
                updateCartBadge();

                System.out.println("Đã thêm vào giỏ hàng: " + auction.getId());
            } else {
                System.out.println("Món hàng này đã có trong giỏ!");
            }
        }
    }

    // ══════════════════════════════════════════
    // Search
    // ══════════════════════════════════════════
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;
        switchTab(tabShop, "Shop");
        txtShopSearch.setText(keyword);
    }

    // ══════════════════════════════════════════
    // Cart
    // ══════════════════════════════════════════
    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Giỏ hàng trống",
                    "Vui lòng thêm sản phẩm trước khi thanh toán.");
            return;
        }
        // TODO: submit order to server
        showAlert(Alert.AlertType.INFORMATION, "Đặt hàng thành công",
                "Đơn hàng của bạn đã được ghi nhận!");
        cartItems.clear();
        cartCount = 0;
        updateCartBadge();
        updateCartSummary();
    }

    private void updateCartSummary() {
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        double shipping = cartItems.isEmpty() ? 0 : 30_000;
        lblSubtotal.setText(String.format("%,.0f ₫", subtotal));
        lblShipping.setText(String.format("%,.0f ₫", shipping));
        lblCartTotal.setText(String.format("%,.0f ₫", subtotal + shipping));
    }

    // ══════════════════════════════════════════
    // Notification
    // ══════════════════════════════════════════
    @FXML private void handleMarkAllRead()   { if (lblNotifCount != null) lblNotifCount.setText("0"); }
    @FXML private void filterNotifAll()      { /* TODO */ }
    @FXML private void filterNotifUnread()   { /* TODO */ }
    @FXML private void filterNotifAuction()  { /* TODO */ }
    @FXML private void filterNotifOrder()    { /* TODO */ }

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

    // ══════════════════════════════════════════
    // Inner model classes
    // ══════════════════════════════════════════

    public static class CartItem {
        private final String name;
        private final double price;
        private final int    quantity;

        public CartItem(String name, double price, int quantity) {
            this.name     = name;
            this.price    = price;
            this.quantity = quantity;
        }

        public String getName()     { return name; }
        public double getPrice()    { return price; }
        public int    getQuantity() { return quantity; }
        public double getTotal()    { return price * quantity; }
    }

    public static class OrderItem {
        private final String orderId;
        private final String date;
        private final String items;
        private final double total;
        private final String status;

        public OrderItem(String orderId, String date, String items, double total, String status) {
            this.orderId = orderId;
            this.date    = date;
            this.items   = items;
            this.total   = total;
            this.status  = status;
        }

        public String getOrderId() { return orderId; }
        public String getDate()    { return date; }
        public String getItems()   { return items; }
        public double getTotal()   { return total; }
        public String getStatus()  { return status; }
    }
}

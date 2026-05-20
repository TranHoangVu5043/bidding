package Client.controller;

import Client.model.Auction;
import Client.model.Bid;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.BidApi;
import Client.networking.endpoints.UserApi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class UserController {

    // ── Top Navbar ──
    @FXML private TextField txtSearch;
    @FXML private Button    btnCartTop;
    @FXML private Label     lblCartCount;
    @FXML private Button    btnNotifTop;
    @FXML private Label     lblNotifCount;
    @FXML private Label     lblUsername;
    @FXML private Circle    avatarCircle;

    // ── Sidebar ──
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

    // ── TabPane ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabShop;
    @FXML private Tab tabAuction;
    @FXML private Tab tabCart;
    @FXML private Tab tabOrderHistory;
    @FXML private Tab tabNotification;
    @FXML private Tab tabProfile;
    @FXML private Tab tabSettings;

    // ── Dashboard ──
    @FXML private Label            lblActiveBids;
    @FXML private Label            lblWonAuctions;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private FlowPane         auctionFlowPane;

    // ── Shop ──
    @FXML private TextField        txtShopSearch;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbSort;
    @FXML private FlowPane         shopFlowPane;

    // ── Auction ──
    @FXML private TextField                     txtAuctionSearch;
    @FXML private ComboBox<String>              cmbAuctionFilter;
    @FXML private Label                         lblMyBidsCount;
    @FXML private TableView<Auction>            tableAuctions;
    @FXML private TableColumn<Auction, Integer> colAucId;
    @FXML private TableColumn<Auction, Integer> colAucItem;
    @FXML private TableColumn<Auction, Double>  colAucStartPrice;
    @FXML private TableColumn<Auction, Double>  colAucCurrentPrice;
    @FXML private TableColumn<Auction, String>  colAucEndTime;
    @FXML private TableColumn<Auction, String>  colAucStatus;
    @FXML private Label     lblSelectedAuction;
    @FXML private TextField txtBidAmount;
    @FXML private Button    btnPlaceBid;
    @FXML private TableView<Bid>            tableBidHistory;
    @FXML private TableColumn<Bid, Integer> colBidUser;
    @FXML private TableColumn<Bid, Double>  colBidAmount;
    @FXML private TableColumn<Bid, String>  colBidTime;
    @FXML private TableColumn<Bid, String>  colBidStatus;

    // ── Cart ──
    @FXML private TableView<CartItem>            tableCart;
    @FXML private TableColumn<CartItem, String>  colCartName;
    @FXML private TableColumn<CartItem, Double>  colCartPrice;
    @FXML private TableColumn<CartItem, Integer> colCartQty;
    @FXML private TableColumn<CartItem, Double>  colCartTotal;
    @FXML private TableColumn<CartItem, String>  colCartAction;
    @FXML private Label  lblSubtotal;
    @FXML private Label  lblShipping;
    @FXML private Label  lblCartTotal;
    @FXML private Button btnCheckout;

    // ── Order History ──
    @FXML private ComboBox<String>               cmbOrderStatus;
    @FXML private TextField                      txtOrderSearch;
    @FXML private TableView<OrderItem>           tableOrderHistory;
    @FXML private TableColumn<OrderItem, String> colOrderId;
    @FXML private TableColumn<OrderItem, String> colOrderDate;
    @FXML private TableColumn<OrderItem, String> colOrderItems;
    @FXML private TableColumn<OrderItem, Double> colOrderTotal;
    @FXML private TableColumn<OrderItem, String> colOrderStatus;
    @FXML private TableColumn<OrderItem, String> colOrderDetail;

    // ── Notification ──
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnNotifAll;
    @FXML private Button btnNotifUnread;
    @FXML private Button btnNotifAuction;
    @FXML private Button btnNotifOrder;
    @FXML private VBox   notificationList;

    // ── Profile ──
    @FXML private Label         lblProfileName;
    @FXML private Label         lblProfileEmail;
    @FXML private Button        btnChangeAvatar;
    @FXML private TextField     txtFullName;
    @FXML private TextField     txtPhone;
    @FXML private TextField     txtEmail;
    @FXML private TextField     txtAddress;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;

    // ── Settings ──
    @FXML private Button           toggleAuctionNotif;
    @FXML private Button           toggleOrderNotif;
    @FXML private Button           toggleEmailNotif;
    @FXML private Button           toggle2FA;
    @FXML private ComboBox<String> cmbLanguage;
    @FXML private ComboBox<String> cmbCurrency;
    @FXML private Button           btnDeleteAccount;

    // ── API ──
    private final AuctionApi auctionApi = new AuctionApi();
    private final BidApi     bidApi     = new BidApi();
    private final UserApi    userApi    = new UserApi();

    // ── Data ──
    private final ObservableList<Auction>   allAuctions  = FXCollections.observableArrayList();
    private FilteredList<Auction>           filteredAuctions;
    private final ObservableList<Bid>       bidHistory   = FXCollections.observableArrayList();
    private final ObservableList<CartItem>  cartItems    = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItems   = FXCollections.observableArrayList();
    private FilteredList<OrderItem>         filteredOrders;

    // Danh sách tất cả notification để lọc
    private final ObservableList<VBox> allNotifNodes = FXCollections.observableArrayList();

    private Auction selectedAuction = null;
    private int cartCount = 0;

    // ══════════════════════════════════════════
    // Initialize
    // ══════════════════════════════════════════
    @FXML
    public void initialize() {
        setupAuctionTable();
        setupBidHistoryTable();
        setupCartTable();
        setupOrderTable();
        setupComboBoxes();
        loadCurrentUser();
        loadAuctions();
        highlightButton(btnDashBoard);
    }

    // ══════════════════════════════════════════
    // Setup
    // ══════════════════════════════════════════
    private void setupAuctionTable() {
        colAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAucItem.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colAucStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colAucCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colAucEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colAucStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredAuctions = new FilteredList<>(allAuctions, p -> true);
        tableAuctions.setItems(filteredAuctions);

        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, o, newVal) -> {
            if (newVal != null) {
                selectedAuction = newVal;
                lblSelectedAuction.setText(
                        "Phiên #" + newVal.getId()
                                + "  |  Giá hiện tại: " + String.format("%,.0f ₫", newVal.getCurrentPrice())
                                + "  |  Kết thúc: " + newVal.getEndTime()
                                + "  |  Trạng thái: " + newVal.getStatus()
                );
                loadBidHistory(newVal.getId());
            }
        });
    }

    private void setupBidHistoryTable() {
        colBidUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colBidAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colBidTime.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        tableBidHistory.setItems(bidHistory);
    }

    private void setupCartTable() {
        colCartName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCartPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colCartQty.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colCartTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        // Cột Xóa — nút xóa từng dòng
        colCartAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnRemove = new Button("🗑");
            {
                btnRemove.setStyle("-fx-background-color: #FEE2E2; -fx-text-fill: #B91C1C; "
                        + "-fx-background-radius: 6; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartItems.remove(item);
                    cartCount = Math.max(0, cartCount - item.getQuantity());
                    updateCartBadge();
                    updateCartSummary();
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btnRemove);
            }
        });

        tableCart.setItems(cartItems);
    }

    private void setupOrderTable() {
        colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
        colOrderDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colOrderItems.setCellValueFactory(new PropertyValueFactory<>("items"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Cột Chi tiết
        colOrderDetail.setCellFactory(col -> new TableCell<>() {
            private final Button btnDetail = new Button("Xem");
            {
                btnDetail.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #1D4ED8; "
                        + "-fx-background-radius: 6; -fx-cursor: hand;");
                btnDetail.setOnAction(e -> {
                    OrderItem order = getTableView().getItems().get(getIndex());
                    showAlert(Alert.AlertType.INFORMATION, "Chi tiết đơn hàng #" + order.getOrderId(),
                            "Ngày: " + order.getDate()
                                    + "\nSản phẩm: " + order.getItems()
                                    + "\nTổng: " + String.format("%,.0f ₫", order.getTotal())
                                    + "\nTrạng thái: " + order.getStatus());
                });
            }
            @Override protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                setGraphic(empty ? null : btnDetail);
            }
        });

        filteredOrders = new FilteredList<>(orderItems, p -> true);
        tableOrderHistory.setItems(filteredOrders);

        // Mẫu dữ liệu
        orderItems.addAll(
                new OrderItem("ORD-001", "2026-05-01", "Laptop Gaming", 25000000, "Hoàn thành"),
                new OrderItem("ORD-002", "2026-05-10", "Điện thoại", 8000000, "Đang giao")
        );
    }

    private void setupComboBoxes() {
        if (cmbFilter != null)
            cmbFilter.setItems(FXCollections.observableArrayList("Tất cả", "Đang chạy", "Sắp kết thúc"));

        if (cmbAuctionFilter != null) {
            cmbAuctionFilter.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "UPCOMING", "ENDED"));
            cmbAuctionFilter.valueProperty().addListener((obs, o, n) -> applyAuctionFilter());
        }

        if (cmbCategory != null)
            cmbCategory.setItems(FXCollections.observableArrayList("Tất cả", "ELECTRONICS", "ART", "VEHICLE"));

        if (cmbSort != null)
            cmbSort.setItems(FXCollections.observableArrayList("Mới nhất", "Giá tăng dần", "Giá giảm dần"));

        if (cmbOrderStatus != null) {
            cmbOrderStatus.setItems(FXCollections.observableArrayList(
                    "Tất cả", "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy"));
            cmbOrderStatus.valueProperty().addListener((obs, o, n) -> applyOrderFilter());
        }

        if (txtOrderSearch != null)
            txtOrderSearch.textProperty().addListener((obs, o, n) -> applyOrderFilter());

        if (cmbLanguage != null)
            cmbLanguage.setItems(FXCollections.observableArrayList("Tiếng Việt", "English"));

        if (cmbCurrency != null)
            cmbCurrency.setItems(FXCollections.observableArrayList("VNĐ", "USD"));
    }

    // ══════════════════════════════════════════
    // Load dữ liệu
    // ══════════════════════════════════════════
    private void loadCurrentUser() {
        new Thread(() -> {
            ApiResponse<Client.model.User> res = userApi.getMe();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    String name  = res.getData().getUsername();
                    String email = res.getData().getEmail() != null ? res.getData().getEmail() : "";
                    if (lblUsername     != null) lblUsername.setText(name);
                    if (lblSidebarName  != null) lblSidebarName.setText(name);
                    if (lblProfileName  != null) lblProfileName.setText(name);
                    if (lblProfileEmail != null) lblProfileEmail.setText(email);
                    if (txtFullName     != null) txtFullName.setText(name);
                    if (txtEmail        != null) txtEmail.setText(email);
                }
            });
        }).start();
    }

    private void loadAuctions() {
        new Thread(() -> {
            ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    allAuctions.setAll(res.getData());
                    long active = allAuctions.stream()
                            .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
                    if (lblActiveBids  != null) lblActiveBids.setText(active + " phiên");
                    if (lblMyBidsCount != null) lblMyBidsCount.setText(String.valueOf(allAuctions.size()));
                } else {
                    showAlert(Alert.AlertType.WARNING, "Lỗi", "Không thể tải danh sách đấu giá.");
                }
            });
        }).start();
    }

    private void loadBidHistory(int auctionId) {
        new Thread(() -> {
            ApiResponse<List<Bid>> res = bidApi.getBidHistory(auctionId);
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null)
                    bidHistory.setAll(res.getData());
                else
                    bidHistory.clear();
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Auction — Lọc + Đặt giá
    // ══════════════════════════════════════════
    private void applyAuctionFilter() {
        String kw     = txtAuctionSearch != null ? txtAuctionSearch.getText().toLowerCase().trim() : "";
        String status = cmbAuctionFilter != null ? cmbAuctionFilter.getValue() : null;
        filteredAuctions.setPredicate(a -> {
            boolean matchKw = kw.isEmpty()
                    || String.valueOf(a.getId()).contains(kw)
                    || String.valueOf(a.getItemId()).contains(kw);
            boolean matchSt = status == null || "Tất cả".equals(status)
                    || status.equalsIgnoreCase(a.getStatus());
            return matchKw && matchSt;
        });
    }

    @FXML private void handleAuctionSearch()  { applyAuctionFilter(); }

    @FXML
    private void handleRefreshAuctions() {
        tableAuctions.getSelectionModel().clearSelection();
        selectedAuction = null;
        if (lblSelectedAuction != null) lblSelectedAuction.setText("— Chưa chọn phiên nào —");
        bidHistory.clear();
        loadAuctions();
    }

    @FXML
    private void handlePlaceBid() {
        if (selectedAuction == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn phiên",
                    "Vui lòng chọn một phiên đấu giá từ bảng trước khi đặt giá.");
            return;
        }
        if (!"ACTIVE".equalsIgnoreCase(selectedAuction.getStatus())) {
            showAlert(Alert.AlertType.WARNING, "Phiên không hoạt động",
                    "Phiên này đang ở trạng thái " + selectedAuction.getStatus() + ", không thể đặt giá.");
            return;
        }

        String amountText = txtBidAmount != null ? txtBidAmount.getText().trim() : "";
        if (amountText.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập số tiền muốn đặt.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText.replace(",", ""));
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Sai định dạng", "Số tiền không hợp lệ. Vui lòng nhập số.");
            return;
        }

        if (amount <= selectedAuction.getCurrentPrice()) {
            showAlert(Alert.AlertType.WARNING, "Giá quá thấp",
                    "Giá đặt phải cao hơn giá hiện tại: "
                            + String.format("%,.0f ₫", selectedAuction.getCurrentPrice()));
            return;
        }

        int auctionId = selectedAuction.getId();
        double finalAmt = amount;
        if (btnPlaceBid != null) btnPlaceBid.setDisable(true);

        new Thread(() -> {
            ApiResponse<Void> res = bidApi.placeBid(auctionId, finalAmt);
            Platform.runLater(() -> {
                if (btnPlaceBid != null) btnPlaceBid.setDisable(false);
                if (res.getStatus() == 201) {
                    if (txtBidAmount != null) txtBidAmount.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã đặt giá " + String.format("%,.0f ₫", finalAmt) + " thành công!");
                    loadAuctions();
                    loadBidHistory(auctionId);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại",
                            "Không thể đặt giá: " + res.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Shop — Tìm kiếm
    // ══════════════════════════════════════════
    @FXML
    private void handleSearch() {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        if (keyword.isEmpty()) return;
        switchTab(tabShop);
        highlightButton(btnShop);
        if (txtShopSearch != null) txtShopSearch.setText(keyword);
        // TODO: gọi API tìm sản phẩm theo keyword và hiển thị vào shopFlowPane
    }

    @FXML
    private void handleShopSearch() {
        // TODO: gọi API tìm sản phẩm và render vào shopFlowPane
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

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận đặt hàng");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn đặt hàng với tổng "
                + lblCartTotal.getText() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                // Tạo đơn hàng mới trong lịch sử
                String orderId = "ORD-" + String.format("%03d", orderItems.size() + 1);
                String items   = cartItems.stream().map(CartItem::getName)
                        .reduce((a, b) -> a + ", " + b).orElse("");
                double total   = cartItems.stream().mapToDouble(CartItem::getTotal).sum() + 30000;
                orderItems.add(new OrderItem(orderId,
                        java.time.LocalDate.now().toString(), items, total, "Đang xử lý"));

                cartItems.clear();
                cartCount = 0;
                updateCartBadge();
                updateCartSummary();

                showAlert(Alert.AlertType.INFORMATION, "Đặt hàng thành công",
                        "Đơn hàng " + orderId + " đã được tạo! Xem trong Lịch sử đơn hàng.");
            }
        });
    }

    private void updateCartSummary() {
        double subtotal  = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        double shipping  = cartItems.isEmpty() ? 0 : 30000;
        if (lblSubtotal  != null) lblSubtotal.setText(String.format("%,.0f ₫", subtotal));
        if (lblShipping  != null) lblShipping.setText(String.format("%,.0f ₫", shipping));
        if (lblCartTotal != null) lblCartTotal.setText(String.format("%,.0f ₫", subtotal + shipping));
    }

    private void updateCartBadge() {
        if (lblCartCount != null) lblCartCount.setText(String.valueOf(cartCount));
    }

    // ══════════════════════════════════════════
    // Order History — Lọc
    // ══════════════════════════════════════════
    private void applyOrderFilter() {
        String kw     = txtOrderSearch  != null ? txtOrderSearch.getText().toLowerCase().trim() : "";
        String status = cmbOrderStatus  != null ? cmbOrderStatus.getValue() : null;
        filteredOrders.setPredicate(o -> {
            boolean matchKw = kw.isEmpty()
                    || o.getOrderId().toLowerCase().contains(kw)
                    || o.getItems().toLowerCase().contains(kw);
            boolean matchSt = status == null || "Tất cả".equals(status)
                    || status.equalsIgnoreCase(o.getStatus());
            return matchKw && matchSt;
        });
    }

    // ══════════════════════════════════════════
    // Notification — Filter
    // ══════════════════════════════════════════
    @FXML private void handleMarkAllRead() {
        if (lblNotifCount != null) lblNotifCount.setText("0");
        setNotifButtonActive(btnNotifAll);
    }

    @FXML private void filterNotifAll() {
        setNotifButtonActive(btnNotifAll);
        if (notificationList != null)
            notificationList.getChildren().forEach(n -> n.setVisible(true));
    }

    @FXML private void filterNotifUnread() {
        setNotifButtonActive(btnNotifUnread);
        // TODO: đánh dấu read/unread khi có API thông báo
        showAlert(Alert.AlertType.INFORMATION, "Thông báo",
                "Chưa có thông báo chưa đọc.");
    }

    @FXML private void filterNotifAuction() {
        setNotifButtonActive(btnNotifAuction);
        showAlert(Alert.AlertType.INFORMATION, "Thông báo đấu giá",
                "Chưa có thông báo đấu giá mới.");
    }

    @FXML private void filterNotifOrder() {
        setNotifButtonActive(btnNotifOrder);
        showAlert(Alert.AlertType.INFORMATION, "Thông báo đơn hàng",
                "Chưa có thông báo đơn hàng mới.");
    }

    private void setNotifButtonActive(Button active) {
        Button[] all = {btnNotifAll, btnNotifUnread, btnNotifAuction, btnNotifOrder};
        for (Button b : all) {
            if (b == null) continue;
            b.setStyle(b.getStyle()
                    .replace("-fx-background-color: #0066CC;", "-fx-background-color: #F3F4F6;")
                    .replace("-fx-text-fill: white;", "-fx-text-fill: #374151;"));
        }
        if (active != null)
            active.setStyle(active.getStyle()
                    .replace("-fx-background-color: #F3F4F6;", "-fx-background-color: #0066CC;")
                    .replace("-fx-text-fill: #374151;", "-fx-text-fill: white;"));
    }

    // ══════════════════════════════════════════
    // Profile
    // ══════════════════════════════════════════
    @FXML
    private void handleChangeAvatar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh đại diện");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg"));
        File file = chooser.showOpenDialog(btnChangeAvatar.getScene().getWindow());
        if (file != null) {
            // TODO: upload ảnh lên server
            showAlert(Alert.AlertType.INFORMATION, "Thành công",
                    "Đã chọn ảnh: " + file.getName() + "\n(Chức năng upload đang phát triển)");
        }
    }

    @FXML
    private void handleSaveProfile() {
        String name  = txtFullName != null ? txtFullName.getText().trim() : "";
        String phone = txtPhone    != null ? txtPhone.getText().trim()    : "";
        String email = txtEmail    != null ? txtEmail.getText().trim()    : "";

        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng điền ít nhất họ tên và email.");
            return;
        }

        if (!email.contains("@")) {
            showAlert(Alert.AlertType.ERROR, "Email không hợp lệ",
                    "Vui lòng nhập đúng định dạng email.");
            return;
        }

        // TODO: gọi API cập nhật profile
        if (lblUsername     != null) lblUsername.setText(name);
        if (lblSidebarName  != null) lblSidebarName.setText(name);
        if (lblProfileName  != null) lblProfileName.setText(name);
        if (lblProfileEmail != null) lblProfileEmail.setText(email);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin cá nhân.");
    }

    @FXML
    private void handleChangePassword() {
        String oldPw = txtOldPassword     != null ? txtOldPassword.getText()     : "";
        String newPw = txtNewPassword     != null ? txtNewPassword.getText()     : "";
        String cfPw  = txtConfirmPassword != null ? txtConfirmPassword.getText() : "";

        if (oldPw.isEmpty() || newPw.isEmpty() || cfPw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng điền đầy đủ các ô mật khẩu.");
            return;
        }
        if (newPw.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu yếu",
                    "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }
        if (!newPw.equals(cfPw)) {
            showAlert(Alert.AlertType.ERROR, "Không khớp",
                    "Mật khẩu mới và xác nhận không khớp.");
            return;
        }

        // TODO: gọi API đổi mật khẩu
        if (txtOldPassword     != null) txtOldPassword.clear();
        if (txtNewPassword     != null) txtNewPassword.clear();
        if (txtConfirmPassword != null) txtConfirmPassword.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được thay đổi.");
    }

    // ══════════════════════════════════════════
    // Settings
    // ══════════════════════════════════════════
    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa tài khoản");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn xóa tài khoản?\nHành động này KHÔNG THỂ hoàn tác.");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                // TODO: gọi API xóa tài khoản
                new Thread(() -> {
                    userApi.logout();
                    Platform.runLater(Platform::exit);
                }).start();
            }
        });
    }

    @FXML
    private void handleToggleAuctionNotif() { toggleButton(toggleAuctionNotif); }
    @FXML
    private void handleToggleOrderNotif()   { toggleButton(toggleOrderNotif); }
    @FXML
    private void handleToggleEmailNotif()   { toggleButton(toggleEmailNotif); }
    @FXML
    private void handleToggle2FA()          { toggleButton(toggle2FA); }

    private void toggleButton(Button btn) {
        if (btn == null) return;
        boolean isOn = "Bật".equals(btn.getText());
        btn.setText(isOn ? "Tắt" : "Bật");
        btn.setStyle(btn.getStyle()
                .replace(
                        isOn ? "-fx-background-color: #16A34A;" : "-fx-background-color: #9CA3AF;",
                        isOn ? "-fx-background-color: #9CA3AF;" : "-fx-background-color: #16A34A;"
                ));
    }

    // ══════════════════════════════════════════
    // Sidebar Navigation
    // ══════════════════════════════════════════
    @FXML private void handleDashBoard()    { switchTab(tabDashboard);    highlightButton(btnDashBoard);    loadAuctions(); }
    @FXML private void handleShop()         { switchTab(tabShop);         highlightButton(btnShop); }
    @FXML private void handleAuction()      { switchTab(tabAuction);      highlightButton(btnAuction);      loadAuctions(); }
    @FXML private void handleCart()         { switchTab(tabCart);         highlightButton(btnCart); }
    @FXML private void handleOrderHistory() { switchTab(tabOrderHistory); highlightButton(btnOrderHistory); }
    @FXML private void handleNotification() { switchTab(tabNotification); highlightButton(btnNotification); }
    @FXML private void handleProfile()      { switchTab(tabProfile);      highlightButton(btnProfile); }
    @FXML private void handleSettings()     { switchTab(tabSettings);     highlightButton(btnSettings); }

    @FXML
    private void handleSignOut() {
        new Thread(() -> {
            userApi.logout();
            Platform.runLater(Platform::exit);
        }).start();
    }

    private void highlightButton(Button active) {
        Button[] all = {btnDashBoard, btnShop, btnAuction, btnCart,
                btnOrderHistory, btnNotification, btnProfile, btnSettings};
        for (Button b : all) {
            if (b == null) continue;
            b.setStyle(b.getStyle()
                    .replace("-fx-background-color: #0066CC;", "-fx-background-color: transparent;")
                    .replace("-fx-text-fill: WHITE;",   "-fx-text-fill: #CBD5E1;")
                    .replace("-fx-text-fill: white;",   "-fx-text-fill: #CBD5E1;"));
        }
        if (active != null)
            active.setStyle(active.getStyle()
                    .replace("-fx-background-color: transparent;", "-fx-background-color: #0066CC;")
                    .replace("-fx-text-fill: #CBD5E1;", "-fx-text-fill: white;"));
    }

    // ══════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════
    private void switchTab(Tab tab) {
        if (mainTabPane != null && tab != null)
            mainTabPane.getSelectionModel().select(tab);
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
            this.name = name; this.price = price; this.quantity = quantity;
        }
        public String getName()     { return name; }
        public double getPrice()    { return price; }
        public int    getQuantity() { return quantity; }
        public double getTotal()    { return price * quantity; }
    }

    public static class OrderItem {
        private final String orderId, date, items, status;
        private final double total;
        public OrderItem(String orderId, String date, String items, double total, String status) {
            this.orderId = orderId; this.date = date; this.items = items;
            this.total = total; this.status = status;
        }
        public String getOrderId() { return orderId; }
        public String getDate()    { return date; }
        public String getItems()   { return items; }
        public double getTotal()   { return total; }
        public String getStatus()  { return status; }
    }
}
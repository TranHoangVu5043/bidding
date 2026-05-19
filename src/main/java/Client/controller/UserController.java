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

import java.util.List;

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
    // FXML — Sidebar
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

    // ══════════════════════════════════════════
    // FXML — TabPane
    // ══════════════════════════════════════════
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabShop;
    @FXML private Tab tabAuction;
    @FXML private Tab tabCart;
    @FXML private Tab tabOrderHistory;
    @FXML private Tab tabNotification;
    @FXML private Tab tabProfile;
    @FXML private Tab tabSettings;

    // ══════════════════════════════════════════
    // FXML — Tab Dashboard
    // ══════════════════════════════════════════
    @FXML private Label              lblActiveBids;
    @FXML private Label              lblWonAuctions;
    @FXML private ComboBox<String>   cmbFilter;
    @FXML private FlowPane           auctionFlowPane;

    // ══════════════════════════════════════════
    // FXML — Tab Shop
    // ══════════════════════════════════════════
    @FXML private TextField        txtShopSearch;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbSort;
    @FXML private FlowPane         shopFlowPane;

    // ══════════════════════════════════════════
    // FXML — Tab Auction
    // ══════════════════════════════════════════
    @FXML private TextField                    txtAuctionSearch;
    @FXML private ComboBox<String>             cmbAuctionFilter;
    @FXML private Label                        lblMyBidsCount;

    @FXML private TableView<Auction>           tableAuctions;
    @FXML private TableColumn<Auction, Integer> colAucId;
    @FXML private TableColumn<Auction, String> colAucItem;
    @FXML private TableColumn<Auction, Double> colAucStartPrice;
    @FXML private TableColumn<Auction, Double> colAucCurrentPrice;
    @FXML private TableColumn<Auction, String> colAucEndTime;
    @FXML private TableColumn<Auction, String> colAucStatus;

    @FXML private Label     lblSelectedAuction;
    @FXML private TextField txtBidAmount;
    @FXML private Button    btnPlaceBid;

    @FXML private TableView<Bid>           tableBidHistory;
    @FXML private TableColumn<Bid, String> colBidUser;
    @FXML private TableColumn<Bid, Double> colBidAmount;
    @FXML private TableColumn<Bid, String> colBidTime;
    @FXML private TableColumn<Bid, String> colBidStatus;

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
    // Data & API
    // ══════════════════════════════════════════
    private final AuctionApi auctionApi = new AuctionApi();
    private final BidApi     bidApi     = new BidApi();
    private final UserApi    userApi    = new UserApi();

    private final ObservableList<Auction>   allAuctions  = FXCollections.observableArrayList();
    private FilteredList<Auction>           filteredAuctions;
    private final ObservableList<Bid>       bidHistory   = FXCollections.observableArrayList();
    private final ObservableList<CartItem>  cartItems    = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItems   = FXCollections.observableArrayList();

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
        updateCartBadge();
        loadCurrentUser();
        loadAuctions();
    }

    // ══════════════════════════════════════════
    // Setup Tables
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

        // Khi chọn 1 phiên → hiển thị tên + load bid history
        tableAuctions.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedAuction = newVal;
                lblSelectedAuction.setText("Phiên #" + newVal.getId() + " — Giá hiện tại: "
                        + String.format("%,.0f ₫", newVal.getCurrentPrice()));
                loadBidHistory(newVal.getId());
            }
        });
    }

    private void setupBidHistoryTable() {
        colBidUser.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colBidAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colBidTime.setCellValueFactory(new PropertyValueFactory<>("creatAt"));
        colBidStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableBidHistory.setItems(bidHistory);
    }

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
        cmbFilter.setItems(FXCollections.observableArrayList("Tất cả", "Đang chạy", "Sắp kết thúc"));
        cmbAuctionFilter.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "UPCOMING", "ENDED"));
        cmbCategory.setItems(FXCollections.observableArrayList("Tất cả", "ELECTRONICS", "ART", "VEHICLE"));
        cmbSort.setItems(FXCollections.observableArrayList("Mới nhất", "Giá tăng dần", "Giá giảm dần"));
        cmbOrderStatus.setItems(FXCollections.observableArrayList("Tất cả", "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy"));
        cmbLanguage.setItems(FXCollections.observableArrayList("Tiếng Việt", "English"));
        cmbCurrency.setItems(FXCollections.observableArrayList("VNĐ", "USD"));

        // Lọc theo filter combobox
        cmbAuctionFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyAuctionFilter());
    }

    // ══════════════════════════════════════════
    // Load dữ liệu người dùng
    // ══════════════════════════════════════════
    private void loadCurrentUser() {
        new Thread(() -> {
            ApiResponse<Client.model.User> res = userApi.getMe();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    String name = res.getData().getUsername();
                    lblUsername.setText(name);
                    lblSidebarName.setText(name);
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Auction — Load danh sách
    // ══════════════════════════════════════════
    private void loadAuctions() {
        new Thread(() -> {
            ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    allAuctions.setAll(res.getData());

                    long active = allAuctions.stream()
                            .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
                    lblActiveBids.setText(active + " phiên");
                    lblMyBidsCount.setText(String.valueOf(allAuctions.size()));
                } else {
                    showAlert(Alert.AlertType.WARNING, "Lỗi", "Không thể tải danh sách đấu giá: " + res.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Auction — Lọc
    // ══════════════════════════════════════════
    private void applyAuctionFilter() {
        String keyword = txtAuctionSearch != null ? txtAuctionSearch.getText().toLowerCase().trim() : "";
        String status  = cmbAuctionFilter != null ? cmbAuctionFilter.getValue() : null;

        filteredAuctions.setPredicate(auction -> {
            boolean matchKeyword = keyword.isEmpty()
                    || String.valueOf(auction.getId()).contains(keyword)
                    || String.valueOf(auction.getItemId()).contains(keyword);
            boolean matchStatus = status == null || "Tất cả".equals(status)
                    || status.equalsIgnoreCase(auction.getStatus());
            return matchKeyword && matchStatus;
        });
    }

    @FXML
    private void handleAuctionSearch() {
        applyAuctionFilter();
    }

    @FXML
    private void handleRefreshAuctions() {
        tableAuctions.getSelectionModel().clearSelection();
        selectedAuction = null;
        lblSelectedAuction.setText("— Chưa chọn phiên nào —");
        bidHistory.clear();
        loadAuctions();
    }

    // ══════════════════════════════════════════
    // Auction — Load lịch sử bid
    // ══════════════════════════════════════════
    private void loadBidHistory(int auctionId) {
        new Thread(() -> {
            ApiResponse<List<Bid>> res = bidApi.getBidHistory(auctionId);
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    bidHistory.setAll(res.getData());
                } else {
                    bidHistory.clear();
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Auction — Đặt giá
    // ══════════════════════════════════════════
    @FXML
    private void handlePlaceBid() {
        if (selectedAuction == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn phiên", "Vui lòng chọn một phiên đấu giá từ bảng.");
            return;
        }

        String amountText = txtBidAmount.getText().trim();
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
                    "Giá đặt phải cao hơn giá hiện tại: " + String.format("%,.0f ₫", selectedAuction.getCurrentPrice()));
            return;
        }

        int auctionId = selectedAuction.getId();
        double finalAmount = amount;

        btnPlaceBid.setDisable(true);
        new Thread(() -> {
            ApiResponse<Void> res = bidApi.placeBid(auctionId, finalAmount);
            Platform.runLater(() -> {
                btnPlaceBid.setDisable(false);
                if (res.getStatus() == 201) {
                    txtBidAmount.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã đặt giá " + String.format("%,.0f ₫", finalAmount) + " thành công!");
                    loadAuctions();
                    loadBidHistory(auctionId);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Thất bại", "Không thể đặt giá: " + res.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Sidebar Navigation
    // ══════════════════════════════════════════
    @FXML private void handleDashBoard()    { switchTab(tabDashboard,    "Dashboard");         loadAuctions(); }
    @FXML private void handleShop()         { switchTab(tabShop,         "Shop"); }
    @FXML private void handleAuction()      { switchTab(tabAuction,      "Sàn Đấu Giá");       loadAuctions(); }
    @FXML private void handleCart()         { switchTab(tabCart,         "Giỏ Hàng"); }
    @FXML private void handleOrderHistory() { switchTab(tabOrderHistory, "Lịch Sử Đơn Hàng"); }
    @FXML private void handleNotification() { switchTab(tabNotification, "Thông Báo"); }
    @FXML private void handleProfile()      { switchTab(tabProfile,      "Hồ Sơ Cá Nhân"); }
    @FXML private void handleSettings()     { switchTab(tabSettings,     "Cài Đặt"); }

    @FXML
    private void handleSignOut() {
        new Thread(() -> {
            userApi.logout();
            Platform.runLater(() -> Platform.exit());
        }).start();
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
            showAlert(Alert.AlertType.WARNING, "Giỏ hàng trống", "Vui lòng thêm sản phẩm trước khi thanh toán.");
            return;
        }
        // TODO: gửi đơn hàng lên server
        showAlert(Alert.AlertType.INFORMATION, "Đặt hàng thành công", "Đơn hàng của bạn đã được ghi nhận!");
        cartItems.clear();
        cartCount = 0;
        updateCartBadge();
        updateCartSummary();
    }

    private void updateCartSummary() {
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        double shipping = cartItems.isEmpty() ? 0 : 30000;
        lblSubtotal.setText(String.format("%,.0f ₫", subtotal));
        lblShipping.setText(String.format("%,.0f ₫", shipping));
        lblCartTotal.setText(String.format("%,.0f ₫", subtotal + shipping));
    }

    private void updateCartBadge() {
        lblCartCount.setText(String.valueOf(cartCount));
    }

    // ══════════════════════════════════════════
    // Notification
    // ══════════════════════════════════════════
    @FXML private void handleMarkAllRead()  { lblNotifCount.setText("0"); }
    @FXML private void filterNotifAll()     { /* TODO: lọc tất cả */ }
    @FXML private void filterNotifUnread()  { /* TODO: lọc chưa đọc */ }
    @FXML private void filterNotifAuction() { /* TODO: lọc đấu giá */ }
    @FXML private void filterNotifOrder()   { /* TODO: lọc đơn hàng */ }

    // ══════════════════════════════════════════
    // Profile
    // ══════════════════════════════════════════
    @FXML
    private void handleSaveProfile() {
        String name  = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ họ tên, số điện thoại và email.");
            return;
        }
        lblUsername.setText(name);
        lblSidebarName.setText(name);
        lblProfileName.setText(name);
        lblProfileEmail.setText(email);
        // TODO: gửi lên server
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thông tin cá nhân đã được cập nhật.");
    }

    @FXML
    private void handleChangePassword() {
        String oldPw     = txtOldPassword.getText();
        String newPw     = txtNewPassword.getText();
        String confirmPw = txtConfirmPassword.getText();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng điền đầy đủ các ô mật khẩu.");
            return;
        }
        if (!newPw.equals(confirmPw)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới và xác nhận không khớp.");
            return;
        }
        // TODO: gửi lên server
        txtOldPassword.clear();
        txtNewPassword.clear();
        txtConfirmPassword.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Mật khẩu đã được thay đổi.");
    }

    @FXML private void handleChangeAvatar() { /* TODO: mở dialog chọn ảnh */ }

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
                // TODO: gửi yêu cầu xóa tài khoản lên server
                Platform.exit();
            }
        });
    }

    // ══════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════
    private void switchTab(Tab tab, String title) {
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

package Client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

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
    // FXML — Tab Dashboard
    // ══════════════════════════════════════════
    @FXML private Label     lblActiveBids;
    @FXML private Label     lblWonAuctions;
    @FXML private ComboBox<String> cmbFilter;
    @FXML private FlowPane  auctionFlowPane;

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
    @FXML private TableView<CartItem>             tableCart;
    @FXML private TableColumn<CartItem, String>   colCartImage;
    @FXML private TableColumn<CartItem, String>   colCartName;
    @FXML private TableColumn<CartItem, Double>   colCartPrice;
    @FXML private TableColumn<CartItem, Integer>  colCartQty;
    @FXML private TableColumn<CartItem, Double>   colCartTotal;
    @FXML private TableColumn<CartItem, String>   colCartAction;
    @FXML private Label  lblSubtotal;
    @FXML private Label  lblShipping;
    @FXML private Label  lblCartTotal;
    @FXML private Button btnCheckout;
    @FXML private Button btnContinueShopping;

    // ══════════════════════════════════════════
    // FXML — Tab Order History
    // ══════════════════════════════════════════
    @FXML private ComboBox<String>                    cmbOrderStatus;
    @FXML private TextField                           txtOrderSearch;
    @FXML private TableView<OrderItem>                tableOrderHistory;
    @FXML private TableColumn<OrderItem, String>      colOrderId;
    @FXML private TableColumn<OrderItem, String>      colOrderDate;
    @FXML private TableColumn<OrderItem, String>      colOrderItems;
    @FXML private TableColumn<OrderItem, Double>      colOrderTotal;
    @FXML private TableColumn<OrderItem, String>      colOrderStatus;
    @FXML private TableColumn<OrderItem, String>      colOrderDetail;

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
    @FXML private Button        toggleAuctionNotif;
    @FXML private Button        toggleOrderNotif;
    @FXML private Button        toggleEmailNotif;
    @FXML private Button        toggle2FA;
    @FXML private ComboBox<String> cmbLanguage;
    @FXML private ComboBox<String> cmbCurrency;
    @FXML private Button        btnDeleteAccount;

    // ══════════════════════════════════════════
    // Data
    // ══════════════════════════════════════════
    private ObservableList<CartItem>  cartItems  = FXCollections.observableArrayList();
    private ObservableList<OrderItem> orderItems = FXCollections.observableArrayList();
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

        // Dashboard stats mẫu — sau thay bằng dữ liệu từ server
        lblActiveBids.setText("2 phiên");
        lblWonAuctions.setText("5 phiên");
        lblUsername.setText("Nguyen Van A");
        lblSidebarName.setText("Nguyen Van A");
    }

    // ══════════════════════════════════════════
    // Setup
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
        cmbFilter.setItems(FXCollections.observableArrayList("Tất cả", "Đang chạy", "Sắp kết thúc"));
        cmbCategory.setItems(FXCollections.observableArrayList("Tất cả", "Điện tử", "Thời trang", "Đồ gia dụng"));
        cmbSort.setItems(FXCollections.observableArrayList("Mới nhất", "Giá tăng dần", "Giá giảm dần"));
        cmbOrderStatus.setItems(FXCollections.observableArrayList("Tất cả", "Đang xử lý", "Đang giao", "Hoàn thành", "Đã hủy"));
        cmbLanguage.setItems(FXCollections.observableArrayList("Tiếng Việt", "English"));
        cmbCurrency.setItems(FXCollections.observableArrayList("VNĐ", "USD"));
    }

    private void updateCartBadge() {
        lblCartCount.setText(String.valueOf(cartCount));
    }

    // ══════════════════════════════════════════
    // Sidebar Navigation
    // ══════════════════════════════════════════
    @FXML private void handleDashBoard()    { switchTab(tabDashboard,     "Dashboard"); }
    @FXML private void handleShop()         { switchTab(tabShop,          "Shop"); }
    @FXML private void handleAuction()      { switchTab(tabDashboard,     "Sàn Đấu Giá"); }
    @FXML private void handleCart()         { switchTab(tabCart,          "Giỏ Hàng"); }
    @FXML private void handleOrderHistory() { switchTab(tabOrderHistory,  "Lịch Sử Đơn Hàng"); }
    @FXML private void handleNotification() { switchTab(tabNotification,  "Thông Báo"); }
    @FXML private void handleProfile()      { switchTab(tabProfile,       "Hồ Sơ Cá Nhân"); }
    @FXML private void handleSettings()     { switchTab(tabSettings,      "Cài Đặt"); }

    @FXML
    private void handleSignOut() {
        System.exit(0);
    }

    // ══════════════════════════════════════════
    // Search
    // ══════════════════════════════════════════
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim();
        if (keyword.isEmpty()) return;
        // Chuyển sang tab Shop và lọc theo keyword
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

    // ══════════════════════════════════════════
    // Notification
    // ══════════════════════════════════════════
    @FXML private void handleMarkAllRead()    { /* TODO: đánh dấu đã đọc tất cả */ lblNotifCount.setText("0"); }
    @FXML private void filterNotifAll()       { /* TODO: lọc tất cả thông báo */ }
    @FXML private void filterNotifUnread()    { /* TODO: lọc chưa đọc */ }
    @FXML private void filterNotifAuction()   { /* TODO: lọc thông báo đấu giá */ }
    @FXML private void filterNotifOrder()     { /* TODO: lọc thông báo đơn hàng */ }

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
        // Cập nhật tên hiển thị
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
        // TODO: gửi yêu cầu đổi mật khẩu lên server
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
                System.exit(0);
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
    // Inner model classes (dùng tạm trong Client)
    // Sau này thay bằng class riêng ở Server/model
    // ══════════════════════════════════════════

    public static class CartItem {
        private String  name;
        private double  price;
        private int     quantity;

        public CartItem(String name, double price, int quantity) {
            this.name     = name;
            this.price    = price;
            this.quantity = quantity;
        }

        public String  getName()     { return name; }
        public double  getPrice()    { return price; }
        public int     getQuantity() { return quantity; }
        public double  getTotal()    { return price * quantity; }
    }

    public static class OrderItem {
        private String orderId;
        private String date;
        private String items;
        private double total;
        private String status;

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
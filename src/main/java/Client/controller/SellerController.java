package Client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class SellerController {

    // ── Inventory tab ──
    @FXML private TableView<Product> tableView;
    @FXML private TableColumn<Product, String>  colName;
    @FXML private TableColumn<Product, Double>  colPrice;
    @FXML private TableColumn<Product, Integer> colStock;
    @FXML private TableColumn<Product, String>  colStatus;

    // ── Add-product tab ──
    @FXML private TextField txtName;
    @FXML private TextField txtPrice;
    @FXML private TextField txtStock;

    // ── Search field (trong tab Inventory) ──
    @FXML private TextField txtSearch;

    // ── TabPane để switch tab khi bấm sidebar ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabInventory;
    @FXML private Tab tabAddProduct;
    @FXML private Tab tabAuctions;
    @FXML private Tab tabOrders;
    @FXML private Tab tabRevenue;
    @FXML private Tab tabNotification;
    @FXML private Tab tabHistory;
    @FXML private Tab tabProfile;

    @FXML private Label lblPageTitle;

    // FIX: dùng FilteredList thay vì tạo ObservableList mới mỗi lần search
    private ObservableList<Product> masterData = FXCollections.observableArrayList();
    private FilteredList<Product>   filteredData;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // FIX: wrap masterData trong FilteredList
        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);

        // Lắng nghe realtime khi gõ vào txtSearch
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
        }
    }

    // ── Add product ──
    @FXML
    private void handleAddProduct() {
        try {
            String name     = txtName.getText().trim();
            String priceStr = txtPrice.getText().trim();
            String stockStr = txtStock.getText().trim();

            if (name.isEmpty() || priceStr.isEmpty() || stockStr.isEmpty()) {
                showAlert("Thiếu thông tin", "Vui lòng điền đầy đủ tên, giá và số lượng.");
                return;
            }

            double price = Double.parseDouble(priceStr);
            int    stock = Integer.parseInt(stockStr);

            masterData.add(new Product(name, price, stock, "Active"));
            clearFields();

            // Chuyển sang tab Inventory để thấy ngay sản phẩm vừa thêm
            showInventory();

        } catch (NumberFormatException e) {
            showAlert("Dữ liệu không hợp lệ", "Giá và số lượng phải là số.");
        }
    }

    // ── Search ──
    @FXML
    private void handleSearch() {
        applyFilter(txtSearch != null ? txtSearch.getText() : "");
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();
        filteredData.setPredicate(p ->
                kw.isEmpty() || p.getName().toLowerCase().contains(kw)
        );
    }

    @FXML
    private void showCancel() { clearFields(); }

    private void clearFields() {
        if (txtName  != null) txtName.clear();
        if (txtPrice != null) txtPrice.clear();
        if (txtStock != null) txtStock.clear();
    }

    // ── FIX: Implement đầy đủ các hàm nav sidebar ──

    @FXML
    public void showDashboard() {
        switchTab(tabDashboard, "Dashboard");
    }

    @FXML
    public void showInventory() {
        switchTab(tabInventory, "Kho Hàng");
    }

    @FXML
    public void showAddProduct() {
        switchTab(tabAddProduct, "Thêm Sản Phẩm");
    }

    @FXML
    public void showAuctions() {
        switchTab(tabAuctions, "Đấu Giá");
    }

    @FXML
    public void showOrders() {
        switchTab(tabOrders, "Đơn Hàng");
    }

    @FXML
    public void showRevenue() {
        switchTab(tabRevenue, "Doanh Thu");
    }

    @FXML
    public void showNotification() {
        switchTab(tabNotification, "Thông Báo");
    }

    @FXML
    public void showHistory() {
        switchTab(tabHistory, "Lịch Sử");
    }

    @FXML
    public void showProfile() {
        switchTab(tabProfile, "Hồ Sơ");
    }

    @FXML
    public void showLogout() {
        System.exit(0);
    }

    // ── Các handler FXML khác ──
    @FXML private void handleSaveShop() {}
    @FXML private void handleChangePw() {}
    @FXML private void handleUploadImage() {}

    // ── Helpers ──

    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) {
            mainTabPane.getSelectionModel().select(tab);
        }
        if (lblPageTitle != null && title != null) {
            lblPageTitle.setText(title);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

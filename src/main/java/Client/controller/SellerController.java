package Client.controller;

import Client.model.Item;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class SellerController {

    // ── Inventory tab ──
    @FXML private TableView<Item>           tableView;
    @FXML private TableColumn<Item, String> colName;
    @FXML private TableColumn<Item, String> colCategory;
    @FXML private TableColumn<Item, String> colCondition;
    @FXML private TableColumn<Item, String> colStatus;

    // ── Add-item tab ──
    @FXML private TextField     txtName;
    @FXML private TextField     txtDescription;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbCondition;

    // ── Search ──
    @FXML private TextField txtSearch;

    // ── Navigation ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabInventory;
    @FXML private Tab tabAddItem;
    @FXML private Tab tabAuctions;
    @FXML private Tab tabOrders;
    @FXML private Tab tabRevenue;
    @FXML private Tab tabNotification;
    @FXML private Tab tabHistory;
    @FXML private Tab tabProfile;
    @FXML private Label lblPageTitle;

    // Controllers own their API objects — JavaFX cannot inject constructor args
    private final ItemApi    itemApi    = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();

    private final ObservableList<Item> masterData   = FXCollections.observableArrayList();
    private FilteredList<Item>         filteredData;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);

        if (cmbCategory != null) {
            cmbCategory.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        }
        if (cmbCondition != null) {
            cmbCondition.setItems(FXCollections.observableArrayList("NEW", "USED", "REFURBISHED"));
        }

        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
        }

        loadMyItems();
    }

    // ── Load items from server ──
    private void loadMyItems() {
        new Thread(() -> {
            ApiResponse<List<Item>> response = itemApi.getMyItems();
            Platform.runLater(() -> {
                if (response.getStatus() == 200 && response.getData() != null) {
                    masterData.setAll(response.getData());
                } else {
                    showAlert("Lỗi", "Không thể tải danh sách sản phẩm: " + response.getMessage());
                }
            });
        }).start();
    }

    // ── Add item ──
    @FXML
    private void handleAddItem() {
        String name        = txtName != null ? txtName.getText().trim() : "";
        String description = txtDescription != null ? txtDescription.getText().trim() : "";
        String category    = cmbCategory != null ? cmbCategory.getValue() : null;
        String condition   = cmbCondition != null ? cmbCondition.getValue() : null;

        if (name.isEmpty() || category == null || condition == null) {
            showAlert("Thiếu thông tin", "Vui lòng điền tên, chọn danh mục và tình trạng.");
            return;
        }

        new Thread(() -> {
            ApiResponse<Void> response = itemApi.createItem(name, description, category, condition);
            Platform.runLater(() -> {
                if (response.getStatus() == 201) {
                    clearFields();
                    loadMyItems();
                    showInventory();
                } else {
                    showAlert("Lỗi", "Không thể thêm sản phẩm: " + response.getMessage());
                }
            });
        }).start();
    }

    // ── Search ──
    @FXML
    private void handleSearch() {
        applyFilter(txtSearch != null ? txtSearch.getText() : "");
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();
        filteredData.setPredicate(item ->
                kw.isEmpty() || item.getName().toLowerCase().contains(kw)
        );
    }

    @FXML private void showCancel() { clearFields(); }

    private void clearFields() {
        if (txtName        != null) txtName.clear();
        if (txtDescription != null) txtDescription.clear();
        if (cmbCategory    != null) cmbCategory.setValue(null);
        if (cmbCondition   != null) cmbCondition.setValue(null);
    }

    // ── Sidebar navigation ──
    @FXML public void showDashboard()    { switchTab(tabDashboard,    "Dashboard");      loadMyItems(); }
    @FXML public void showInventory()    { switchTab(tabInventory,    "Kho Hàng");       loadMyItems(); }
    @FXML public void showAddItem()      { switchTab(tabAddItem,      "Thêm Sản Phẩm"); }
    @FXML public void showAuctions()     { switchTab(tabAuctions,     "Đấu Giá"); }
    @FXML public void showOrders()       { switchTab(tabOrders,       "Đơn Hàng"); }
    @FXML public void showRevenue()      { switchTab(tabRevenue,      "Doanh Thu"); }
    @FXML public void showNotification() { switchTab(tabNotification, "Thông Báo"); }
    @FXML public void showHistory()      { switchTab(tabHistory,      "Lịch Sử"); }
    @FXML public void showProfile()      { switchTab(tabProfile,      "Hồ Sơ"); }
    @FXML public void showLogout()       { Platform.exit(); }

    @FXML private void handleSaveShop()     {}
    @FXML private void handleChangePw()     {}
    @FXML private void handleUploadImage()  {}

    // ── Helpers ──
    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null && title != null) lblPageTitle.setText(title);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
package Client.controller.seller;

import Client.model.item.Item;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class SellerController {

    // ── Inventory table columns ──
    @FXML private TableView<Item>              tableView;
    @FXML private TableColumn<Item, Integer>   colId;
    @FXML private TableColumn<Item, String>    colName;
    @FXML private TableColumn<Item, String>    colCategory;
    @FXML private TableColumn<Item, String>    colStatus;
    // colPrice and colStock exist in FXML but not in the Item model — declared to avoid
    // FXMLLoader warnings; they stay empty since the server model doesn't expose these fields yet.
    @FXML private TableColumn<Item, ?> colPrice;
    @FXML private TableColumn<Item, ?> colStock;
    @FXML private TableColumn<Item, ?> colActions;

    // ── Add-product tab fields ──
    @FXML private TextField  txtName;
    @FXML private TextArea   txtDescription;  // FIX: was TextField — FXML declares TextArea
    @FXML private TextField  txtPrice;        // present in FXML, captured for future use
    @FXML private TextField  txtStock;        // present in FXML, captured for future use
    @FXML private ComboBox<String> cmbCategory;
    // cmbCondition is not in SellerView.fxml; declared optional to avoid NPE in handleAddProduct
    @FXML private ComboBox<String> cmbCondition;

    // ── Search ──
    @FXML private TextField txtSearch;

    // ── Sidebar buttons ──
    @FXML private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnAddProduct;
    @FXML private Button btnAuctions;
    @FXML private Button btnOrders;
    @FXML private Button btnRevenue;
    @FXML private Button btnNotification;
    @FXML private Button btnHistory;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;  // FIX: declared so SceneUtil.switchToScene() can get the Stage

    // ── Navigation ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabInventory;
    @FXML private Tab tabAddProduct;    // FIX: was tabAddItem — must match FXML fx:id="tabAddProduct"
    @FXML private Tab tabAuctions;
    @FXML private Tab tabOrders;
    @FXML private Tab tabRevenue;
    @FXML private Tab tabNotification;
    @FXML private Tab tabHistory;
    @FXML private Tab tabProfile;
    @FXML private Label lblPageTitle;
    @FXML private Label lblShopName;    // sidebar shop name
    @FXML private Label lblSellerName;  // topbar seller name

    private final ItemApi    itemApi    = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();

    private final ObservableList<Item> masterData   = FXCollections.observableArrayList();
    private FilteredList<Item>         filteredData;

    @FXML
    public void initialize() {
        // ── Table cell factories ──
        if (colId       != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colName     != null) colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colCategory != null) colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (colStatus   != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        // colPrice, colStock, colActions have no matching fields in Item model yet — left without factory

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

        populateSellerInfo();
        loadMyItems();
    }

    private void populateSellerInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        String name = user.getUsername() != null ? user.getUsername() : "Seller";
        if (lblShopName   != null) lblShopName.setText(name);
        if (lblSellerName != null) lblSellerName.setText(name);
    }

    // ── Load items from server ──
    private void loadMyItems() {
        new Thread(() -> {
            ApiResponse<List<Item>> response = itemApi.getMyItems();
            Platform.runLater(() -> {
                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    masterData.setAll(response.getData());
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    showAlert("Lỗi", "Không thể tải danh sách sản phẩm: " + msg);
                }
            });
        }).start();
    }

    // ── Add product ──
    // FIX: method renamed from handleAddItem → handleAddProduct to match FXML onAction="#handleAddProduct"
    @FXML
    private void handleAddProduct() {
        String name        = txtName        != null ? txtName.getText().trim()        : "";
        String description = txtDescription != null ? txtDescription.getText().trim() : "";
        String category    = cmbCategory    != null ? cmbCategory.getValue()          : null;
        // condition is optional — cmbCondition may be null if not present in FXML
        String condition   = cmbCondition   != null ? cmbCondition.getValue()         : "NEW";

        if (name.isEmpty() || category == null) {
            showAlert("Thiếu thông tin", "Vui lòng điền tên sản phẩm và chọn danh mục.");
            return;
        }

        new Thread(() -> {
            ApiResponse<Void> response = itemApi.createItem(name, description, category, condition);
            Platform.runLater(() -> {
                if (response != null && response.getStatus() == 201) {
                    clearFields();
                    loadMyItems();
                    showInventory();
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    showAlert("Lỗi", "Không thể thêm sản phẩm: " + msg);
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

    @FXML private void showCancel() { clearFields(); showInventory(); }

    private void clearFields() {
        if (txtName        != null) txtName.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtPrice       != null) txtPrice.clear();
        if (txtStock       != null) txtStock.clear();
        if (cmbCategory    != null) cmbCategory.setValue(null);
        if (cmbCondition   != null) cmbCondition.setValue(null);
    }

    // ── Sidebar navigation ──
    @FXML public void showDashboard()    { switchTab(tabDashboard,    "Dashboard");       loadMyItems(); }
    @FXML public void showInventory()    { switchTab(tabInventory,    "Kho Hàng");        loadMyItems(); }
    @FXML public void showAddProduct()   { switchTab(tabAddProduct,   "Thêm Sản Phẩm"); }  // FIX: was showAddItem/tabAddItem
    @FXML public void showAuctions()     { switchTab(tabAuctions,     "Đấu Giá"); }
    @FXML public void showOrders()       { switchTab(tabOrders,       "Đơn Hàng"); }
    @FXML public void showRevenue()      { switchTab(tabRevenue,      "Doanh Thu"); }
    @FXML public void showNotification() { switchTab(tabNotification, "Thông Báo"); }
    @FXML public void showHistory()      { switchTab(tabHistory,      "Lịch Sử"); }
    @FXML public void showProfile()      { switchTab(tabProfile,      "Hồ Sơ"); }

    @FXML
    public void showLogout() {

        SessionManager.clear();
        SceneUtil.switchToScene(btnLogout, "/Client/views/LoginView.fxml", "Login");
    }

    @FXML private void handleSaveShop()    {}
    @FXML private void handleChangePw()    {}
    @FXML private void handleUploadImage() {}

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

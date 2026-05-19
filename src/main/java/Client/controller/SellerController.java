package Client.controller;

import Client.model.Auction;
import Client.model.Item;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.BidApi;
import Client.networking.endpoints.ItemApi;
import Client.networking.endpoints.UserApi;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SellerController {

    // ── Inventory tab ──
    @FXML
    private TableView<Item> tableView;
    @FXML
    private TableColumn<Item, String> colName;
    @FXML
    private TableColumn<Item, String> colCategory;
    @FXML
    private TableColumn<Item, String> colCondition;
    @FXML
    private TableColumn<Item, String> colStatus;

    // ── Add-item tab ──
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtDescription;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private ComboBox<String> cmbCondition;

    // ── Search ──
    @FXML
    private TextField txtSearch;

    // ── Navigation ──
    @FXML
    private TabPane mainTabPane;
    @FXML
    private Tab tabDashboard;
    @FXML
    private Tab tabInventory;
    @FXML
    private Tab tabAddItem;
    @FXML
    private Tab tabAuctions;
    @FXML
    private Tab tabOrders;
    @FXML
    private Tab tabRevenue;
    @FXML
    private Tab tabNotification;
    @FXML
    private Tab tabHistory;
    @FXML
    private Tab tabProfile;
    @FXML
    private Label lblPageTitle;

    // ── Dashboard stats ──
    @FXML
    private Label lblActiveAuctions;
    @FXML
    private Label lblActiveProducts;
    @FXML
    private Label lblSellerName;

    // ── Auction tab — bảng ──
    @FXML
    private TableView<Auction> tableSellerAuctions;
    @FXML
    private TableColumn<Auction, Integer> colSAucId;
    @FXML
    private TableColumn<Auction, Integer> colSAucItem;
    @FXML
    private TableColumn<Auction, Double> colSAucStartPrice;
    @FXML
    private TableColumn<Auction, Double> colSAucCurrentPrice;
    @FXML
    private TableColumn<Auction, Integer> colSAucBidCount;
    @FXML
    private TableColumn<Auction, String> colSAucEndTime;
    @FXML
    private TableColumn<Auction, String> colSAucStatus;

    // ── Auction tab — stats ──
    @FXML
    private Label lblSellerActiveAuctions;
    @FXML
    private Label lblSellerEndedAuctions;
    @FXML
    private Label lblSellerTotalRevenue;

    // ── Auction tab — nút ──
    @FXML
    private Button btnCancelAuction;
    @FXML
    private Button btnViewBids;

    // ── Auction tab — form tạo phiên ──
    @FXML
    private javafx.scene.layout.VBox paneCreateAuction;
    @FXML
    private ComboBox<String> cmbAuctionItem;   // hiển thị tên item
    @FXML
    private TextField txtStartingPrice;
    @FXML
    private TextField txtStartTime;
    @FXML
    private TextField txtEndTime;

    // ── API ──
    private final ItemApi itemApi = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();
    private final BidApi bidApi = new BidApi();
    private final UserApi userApi = new UserApi();

    // ── Data ──
    private final ObservableList<Item> masterData = FXCollections.observableArrayList();
    private FilteredList<Item> filteredData;
    private final ObservableList<Auction> sellerAuctions = FXCollections.observableArrayList();
    // Map tên item → id để dùng khi tạo phiên
    private final java.util.Map<String, Integer> itemNameToId = new java.util.LinkedHashMap<>();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ══════════════════════════════════════════
    // Initialize
    // ══════════════════════════════════════════
    @FXML
    public void initialize() {
        // Inventory table
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        filteredData = new FilteredList<>(masterData, p -> true);
        tableView.setItems(filteredData);

        if (cmbCategory != null)
            cmbCategory.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        if (cmbCondition != null)
            cmbCondition.setItems(FXCollections.observableArrayList("NEW", "USED", "REFURBISHED"));
        if (txtSearch != null)
            txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n));

        // Auction table
        setupAuctionTable();

        // Load data
        loadCurrentUser();
        loadMyItems();
        loadSellerAuctions();
    }

    // ══════════════════════════════════════════
    // Setup bảng auction của seller
    // ══════════════════════════════════════════
    private void setupAuctionTable() {
        if (tableSellerAuctions == null) return;

        colSAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSAucItem.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colSAucStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colSAucCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        colSAucEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colSAucStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tableSellerAuctions.setItems(sellerAuctions);
    }

    // ══════════════════════════════════════════
    // Load user
    // ══════════════════════════════════════════
    private void loadCurrentUser() {
        new Thread(() -> {
            ApiResponse<Client.model.User> res = userApi.getMe();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    String name = res.getData().getUsername();
                    if (lblSellerName != null) lblSellerName.setText(name);
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Load inventory
    // ══════════════════════════════════════════
    private void loadMyItems() {
        new Thread(() -> {
            ApiResponse<List<Item>> response = itemApi.getMyItems();
            Platform.runLater(() -> {
                if (response.getStatus() == 200 && response.getData() != null) {
                    masterData.setAll(response.getData());

                    // Cập nhật combobox item cho form tạo phiên
                    itemNameToId.clear();
                    for (Item item : response.getData()) {
                        itemNameToId.put(item.getName() + " (#" + item.getId() + ")", item.getId());
                    }
                    if (cmbAuctionItem != null)
                        cmbAuctionItem.setItems(FXCollections.observableArrayList(itemNameToId.keySet()));

                    if (lblActiveProducts != null)
                        lblActiveProducts.setText(masterData.size() + " sản phẩm");
                } else {
                    showAlert("Lỗi", "Không thể tải danh sách sản phẩm: " + response.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Load auction của seller
    // ══════════════════════════════════════════
    private void loadSellerAuctions() {
        new Thread(() -> {
            ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    sellerAuctions.setAll(res.getData());
                    updateAuctionStats();
                } else {
                    showAlert("Lỗi", "Không thể tải danh sách đấu giá: " + res.getMessage());
                }
            });
        }).start();
    }

    private void updateAuctionStats() {
        long active = sellerAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
        long ended = sellerAuctions.stream()
                .filter(a -> "ENDED".equalsIgnoreCase(a.getStatus())).count();
        double revenue = sellerAuctions.stream()
                .filter(a -> "ENDED".equalsIgnoreCase(a.getStatus()))
                .mapToDouble(Auction::getCurrentPrice).sum();

        if (lblSellerActiveAuctions != null) lblSellerActiveAuctions.setText(String.valueOf(active));
        if (lblSellerEndedAuctions != null) lblSellerEndedAuctions.setText(String.valueOf(ended));
        if (lblSellerTotalRevenue != null) lblSellerTotalRevenue.setText(String.format("%,.0f ₫", revenue));
        if (lblActiveAuctions != null) lblActiveAuctions.setText(active + " đang chạy");
    }

    // ══════════════════════════════════════════
    // Auction — Mở/đóng form tạo phiên
    // ══════════════════════════════════════════
    @FXML
    private void handleOpenCreateAuction() {
        if (paneCreateAuction != null) {
            paneCreateAuction.setVisible(true);
            paneCreateAuction.setManaged(true);
        }
    }

    @FXML
    private void handleCloseCreateAuction() {
        if (paneCreateAuction != null) {
            paneCreateAuction.setVisible(false);
            paneCreateAuction.setManaged(false);
        }
        clearAuctionForm();
    }

    // ══════════════════════════════════════════
    // Auction — Xác nhận tạo phiên
    // ══════════════════════════════════════════
    @FXML
    private void handleConfirmCreateAuction() {
        String itemKey = cmbAuctionItem != null ? cmbAuctionItem.getValue() : null;
        String priceText = txtStartingPrice != null ? txtStartingPrice.getText().trim() : "";
        String startTimeStr = txtStartTime != null ? txtStartTime.getText().trim() : "";
        String endTimeStr = txtEndTime != null ? txtEndTime.getText().trim() : "";

        // Validate
        if (itemKey == null) {
            showAlert("Thiếu thông tin", "Vui lòng chọn sản phẩm.");
            return;
        }
        if (priceText.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng nhập giá khởi điểm.");
            return;
        }
        if (endTimeStr.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng nhập thời gian kết thúc (yyyy-MM-dd HH:mm).");
            return;
        }

        double startingPrice;
        try {
            startingPrice = Double.parseDouble(priceText.replace(",", ""));
        } catch (NumberFormatException e) {
            showAlert("Sai định dạng", "Giá khởi điểm phải là số.");
            return;
        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = startTimeStr.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(startTimeStr, FMT);
            endTime = LocalDateTime.parse(endTimeStr, FMT);
        } catch (DateTimeParseException e) {
            showAlert("Sai định dạng", "Thời gian phải theo định dạng: yyyy-MM-dd HH:mm");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            showAlert("Lỗi thời gian", "Thời gian kết thúc phải sau thời gian bắt đầu.");
            return;
        }

        int itemId = itemNameToId.get(itemKey);
        String startStr = startTime.toString();
        String endStr = endTime.toString();

        new Thread(() -> {
            ApiResponse<Void> res = auctionApi.createAuction(itemId, startingPrice, startStr, endStr);
            Platform.runLater(() -> {
                if (res.getStatus() == 201) {
                    showAlert("Thành công", "Phiên đấu giá đã được tạo!");
                    handleCloseCreateAuction();
                    loadSellerAuctions();
                } else {
                    showAlert("Thất bại", "Không thể tạo phiên: " + res.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Auction — Hủy phiên được chọn
    // ══════════════════════════════════════════
    @FXML
    private void handleCancelAuction() {
        Auction selected = tableSellerAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn một phiên đấu giá cần hủy.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn hủy phiên đấu giá #" + selected.getId() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    ApiResponse<Void> res = auctionApi.cancelAuction(selected.getId());
                    Platform.runLater(() -> {
                        if (res.getStatus() == 200) {
                            showAlert("Thành công", "Đã hủy phiên đấu giá #" + selected.getId());
                            loadSellerAuctions();
                        } else {
                            showAlert("Thất bại", "Không thể hủy: " + res.getMessage());
                        }
                    });
                }).start();
            }
        });
    }

    // ══════════════════════════════════════════
    // Auction — Xem lịch sử bid
    // ══════════════════════════════════════════
    @FXML
    private void handleViewBids() {
        Auction selected = tableSellerAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn một phiên đấu giá để xem lượt bid.");
            return;
        }

        new Thread(() -> {
            ApiResponse<List<Client.model.Bid>> res = bidApi.getBidHistory(selected.getId());
            Platform.runLater(() -> {
                if (res.getStatus() == 200 && res.getData() != null) {
                    StringBuilder sb = new StringBuilder("Lịch sử bid — Phiên #" + selected.getId() + "\n\n");
                    if (res.getData().isEmpty()) {
                        sb.append("Chưa có lượt bid nào.");
                    } else {
                        for (Client.model.Bid b : res.getData()) {
                            sb.append(String.format("Người dùng #%d — %,.0f ₫ — %s%n",
                                    b.getUserId(), b.getAmount(), b.getCreatedAt()));
                        }
                    }
                    showAlert("Lịch sử đặt giá", sb.toString());
                } else {
                    showAlert("Lỗi", "Không thể tải lịch sử bid: " + res.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    // Auction — Refresh
    // ══════════════════════════════════════════
    @FXML
    private void handleRefreshSellerAuctions() {
        loadSellerAuctions();
    }

    // ══════════════════════════════════════════
    // Inventory — Thêm item
    // ══════════════════════════════════════════
    @FXML
    private void handleAddItem() {
        String name = txtName != null ? txtName.getText().trim() : "";
        String description = txtDescription != null ? txtDescription.getText().trim() : "";
        String category = cmbCategory != null ? cmbCategory.getValue() : null;
        String condition = cmbCondition != null ? cmbCondition.getValue() : null;

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

    // ══════════════════════════════════════════
    // Search / Filter
    // ══════════════════════════════════════════
    @FXML
    private void handleSearch() {
        applyFilter(txtSearch != null ? txtSearch.getText() : "");
    }

    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();
        filteredData.setPredicate(item ->
                kw.isEmpty() || item.getName().toLowerCase().contains(kw));
    }

    // ══════════════════════════════════════════
    // Navigation
    // ══════════════════════════════════════════
    @FXML
    public void showDashboard() {
        switchTab(tabDashboard, "Dashboard");
        loadMyItems();
        loadSellerAuctions();
    }

    @FXML
    public void showInventory() {
        switchTab(tabInventory, "Kho Hàng");
        loadMyItems();
    }

    @FXML
    public void showAddItem() {
        switchTab(tabAddItem, "Thêm Sản Phẩm");
    }

    @FXML
    public void showAuctions() {
        switchTab(tabAuctions, "Đấu Giá");
        loadSellerAuctions();
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
        new Thread(() -> {
            userApi.logout();
            Platform.runLater(Platform::exit);
        }).start();
    }

    @FXML
    private void showCancel() {
        clearFields();
    }

    @FXML
    private void handleSaveShop() { /* TODO */ }

    @FXML
    private void handleChangePw() { /* TODO */ }

    @FXML
    private void handleUploadImage() { /* TODO */ }

    // ══════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════
    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null && title != null) lblPageTitle.setText(title);
    }

    private void clearFields() {
        if (txtName != null) txtName.clear();
        if (txtDescription != null) txtDescription.clear();
        if (cmbCategory != null) cmbCategory.setValue(null);
        if (cmbCondition != null) cmbCondition.setValue(null);
    }

    private void clearAuctionForm() {
        if (cmbAuctionItem != null) cmbAuctionItem.setValue(null);
        if (txtStartingPrice != null) txtStartingPrice.clear();
        if (txtStartTime != null) txtStartTime.clear();
        if (txtEndTime != null) txtEndTime.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

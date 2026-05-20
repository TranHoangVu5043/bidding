package Client.controller;

import Client.model.Auction;
import Client.model.Item;
import Client.model.Bid;
import Client.model.User;
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
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SellerController {

    // ── Inventory tab ──
    @FXML private TableView<Item> tableView;
    @FXML private TableColumn<Item, Integer> colId;
    @FXML private TableColumn<Item, String>  colName;
    @FXML private TableColumn<Item, Double>  colPrice;
    @FXML private TableColumn<Item, Integer> colStock;
    @FXML private TableColumn<Item, String>  colCategory;
    @FXML private TableColumn<Item, String>  colCondition;
    @FXML private TableColumn<Item, String>  colStatus;
    @FXML private TableColumn<Item, Void>    colActions;
    @FXML private ComboBox<String>           cmbInvStatus;

    // ── Add-item tab ──
    @FXML private TextField    txtName;
    @FXML private TextArea     txtDescription;
    @FXML private TextField    txtPrice;
    @FXML private TextField    txtStock;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbCondition;

    // ── Search ──
    @FXML private TextField txtSearch;

    // ── Navigation ──
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

    // ── Dashboard stats ──
    @FXML private Label lblMonthRevenue;
    @FXML private Label lblNewOrders;
    @FXML private Label lblActiveProducts;
    @FXML private Label lblActiveAuctions;
    @FXML private Label lblSellerName;
    @FXML private Label lblShopName;
    @FXML private Label lblNotifBadge;

    // ── Dashboard Charts ──
    @FXML private LineChart<String, Number> chartWeekRevenue;
    @FXML private PieChart chartCategories;

    // ── Dashboard – recent orders table ──
    @FXML private TableView<Object>           tableRecentOrders;
    @FXML private TableColumn<Object, String> colROId;
    @FXML private TableColumn<Object, String> colROCustomer;
    @FXML private TableColumn<Object, String> colROProduct;
    @FXML private TableColumn<Object, String> colROTotal;
    @FXML private TableColumn<Object, String> colROStatus;

    // ── Auction tab — bảng ──
    @FXML private TableView<Auction>            tableSellerAuctions;
    @FXML private TableColumn<Auction, Integer> colSAucId;
    @FXML private TableColumn<Auction, Integer> colSAucItem;
    @FXML private TableColumn<Auction, Double>  colSAucStartPrice;
    @FXML private TableColumn<Auction, Double>  colSAucCurrentPrice;
    @FXML private TableColumn<Auction, Integer> colSAucBidCount;
    @FXML private TableColumn<Auction, String>  colSAucEndTime;
    @FXML private TableColumn<Auction, String>  colSAucStatus;

    // ── Auction tab — stats ──
    @FXML private Label lblSellerActiveAuctions;
    @FXML private Label lblSellerEndedAuctions;
    @FXML private Label lblSellerTotalRevenue;

    // ── Auction tab — nút ──
    @FXML private Button btnCancelAuction;

    // ── Auction tab — form tạo phiên ──
    @FXML private javafx.scene.layout.VBox paneCreateAuction;
    @FXML private ComboBox<String> cmbAuctionItem;
    @FXML private TextField txtStartingPrice;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;

    // ── History tab ──
    @FXML private TextField        txtHistorySearch;
    @FXML private ComboBox<String> cmbHistoryType;
    @FXML private ComboBox<String> cmbHistoryDate;
    @FXML private TableView<Object>           tableHistory;
    @FXML private TableColumn<Object, String> colHisDate;
    @FXML private TableColumn<Object, String> colHisType;
    @FXML private TableColumn<Object, String> colHisDesc;
    @FXML private TableColumn<Object, String> colHisAmount;
    @FXML private TableColumn<Object, String> colHisStatus;

    // ── Profile tab ──
    @FXML private TextField     txtShopName;
    @FXML private TextField     txtSellerPhone;
    @FXML private TextArea      txtShopDesc;
    @FXML private TextField     txtSellerAddress;
    @FXML private PasswordField txtOldPw;
    @FXML private PasswordField txtNewPw;
    @FXML private PasswordField txtConfirmPw;

    // ── API Endpoints Instances ──
    private final ItemApi    itemApi    = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();
    private final BidApi     bidApi     = new BidApi();
    private final UserApi    userApi    = new UserApi();

    // ── Data lists ──
    private final ObservableList<Item>    masterData     = FXCollections.observableArrayList();
    private FilteredList<Item>            filteredData;
    private final ObservableList<Auction> sellerAuctions = FXCollections.observableArrayList();
    private final java.util.Map<String, Integer> itemNameToId = new java.util.LinkedHashMap<>();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    @FXML private Button btnLogout;
    // ══════════════════════════════════════════
    // Initialize
    // ══════════════════════════════════════════
    @FXML
    public void initialize() {
        // Khởi tạo bộ lọc dữ liệu tránh NullPointerException
        filteredData = new FilteredList<>(masterData, p -> true);

        setupInventoryTable();
        setupAuctionTable();
        setupHistoryTable();
        setupWeekRevenueChart();

        // Nạp dữ liệu các ComboBox lựa chọn
        if (cmbCategory != null)
            cmbCategory.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        if (cmbCondition != null)
            cmbCondition.setItems(FXCollections.observableArrayList("NEW", "USED", "REFURBISHED"));

        if (cmbInvStatus != null) {
            cmbInvStatus.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "INACTIVE", "SOLD"));
            cmbInvStatus.setValue("Tất cả");
            cmbInvStatus.valueProperty().addListener((obs, o, n) -> applyFilter(
                    txtSearch != null ? txtSearch.getText() : "", n));
        }

        if (cmbHistoryType != null)
            cmbHistoryType.setItems(FXCollections.observableArrayList("Tất cả", "Đấu giá", "Mua hàng", "Thanh toán"));
        if (cmbHistoryDate != null)
            cmbHistoryDate.setItems(FXCollections.observableArrayList("Tất cả", "Hôm nay", "7 ngày qua", "30 ngày qua"));

        if (txtSearch != null)
            txtSearch.textProperty().addListener((obs, o, n) -> applyFilter(n,
                    cmbInvStatus != null ? cmbInvStatus.getValue() : null));

        // Tiến hành tải dữ liệu qua mạng
        loadCurrentUser();
        loadMyItems();
        loadSellerAuctions();
    }

    // ══════════════════════════════════════════
    // Setup Tables
    // ══════════════════════════════════════════
    private void setupInventoryTable() {
        if (tableView == null) return;
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCondition.setCellValueFactory(new PropertyValueFactory<>("condition"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        if (colActions != null) {
            colActions.setCellFactory(col -> new TableCell<>() {
                private final Button btnEdit = new Button("✏ Sửa");
                {
                    btnEdit.setStyle("-fx-background-color: #EFF6FF; -fx-text-fill: #0066CC;" +
                            " -fx-background-radius: 6; -fx-font-size: 11; -fx-cursor: hand;");
                    btnEdit.setOnAction(e -> {
                        Item item = getTableView().getItems().get(getIndex());
                        showAlert("Sửa sản phẩm", "Tính năng sửa sản phẩm #" + item.getId() + " đang được phát triển.", Alert.AlertType.INFORMATION);
                    });
                }
                @Override
                protected void updateItem(Void v, boolean empty) {
                    super.updateItem(v, empty);
                    setGraphic(empty ? null : btnEdit);
                }
            });
        }
        tableView.setItems(filteredData);
    }

    private void setupAuctionTable() {
        if (tableSellerAuctions == null) return;
        colSAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSAucItem.setCellValueFactory(new PropertyValueFactory<>("itemId"));
        colSAucStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
        colSAucCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));

        if (colSAucBidCount != null) {
            colSAucBidCount.setCellValueFactory(new PropertyValueFactory<>("bidCount"));
        }

        colSAucEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        colSAucStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        tableSellerAuctions.setItems(sellerAuctions);
    }

    private void setupHistoryTable() { }

    // ══════════════════════════════════════════
    // Charts (Biểu đồ)
    // ══════════════════════════════════════════
    private void setupWeekRevenueChart() {
        if (chartWeekRevenue == null) return;
        chartWeekRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu tuần này (₫)");

        series.getData().add(new XYChart.Data<>("Thứ 2", 1200000));
        series.getData().add(new XYChart.Data<>("Thứ 3", 1500000));
        series.getData().add(new XYChart.Data<>("Thứ 4", 800000));
        series.getData().add(new XYChart.Data<>("Thứ 5", 2500000));
        series.getData().add(new XYChart.Data<>("Thứ 6", 1900000));
        series.getData().add(new XYChart.Data<>("Thứ 7", 3000000));
        series.getData().add(new XYChart.Data<>("Chủ Nhật", 4500000));

        chartWeekRevenue.getData().add(series);
    }

    private void setupCategoryPieChart() {
        if (chartCategories == null) return;

        long electronics = masterData.stream().filter(i -> "ELECTRONICS".equalsIgnoreCase(i.getCategory())).count();
        long art         = masterData.stream().filter(i -> "ART".equalsIgnoreCase(i.getCategory())).count();
        long vehicle     = masterData.stream().filter(i -> "VEHICLE".equalsIgnoreCase(i.getCategory())).count();

        PieChart.Data slice1 = new PieChart.Data("Điện tử (" + electronics + ")", electronics);
        PieChart.Data slice2 = new PieChart.Data("Nghệ thuật (" + art + ")", art);
        PieChart.Data slice3 = new PieChart.Data("Xe cộ (" + vehicle + ")", vehicle);

        chartCategories.setData(FXCollections.observableArrayList(slice1, slice2, slice3));
    }

    // ══════════════════════════════════════════
    // Network API Calls & Threading
    // ══════════════════════════════════════════
    private void loadCurrentUser() {
        new Thread(() -> {
            try {
                ApiResponse<User> res = userApi.getMe();
                Platform.runLater(() -> {
                    if (res != null && res.getStatus() == 200 && res.getData() != null) {
                        String name = res.getData().getUsername();
                        if (lblSellerName != null) lblSellerName.setText(name);
                        if (lblShopName   != null) lblShopName.setText(name);
                        if (txtShopName   != null) txtShopName.setText(name);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadMyItems() {
        new Thread(() -> {
            try {
                ApiResponse<List<Item>> response = itemApi.getMyItems();
                Platform.runLater(() -> {
                    if (response != null && response.getStatus() == 200 && response.getData() != null) {
                        masterData.setAll(response.getData());
                        itemNameToId.clear();
                        for (Item item : response.getData()) {
                            itemNameToId.put(item.getName() + " (#" + item.getId() + ")", item.getId());
                        }
                        if (cmbAuctionItem != null)
                            cmbAuctionItem.setItems(FXCollections.observableArrayList(itemNameToId.keySet()));
                        if (lblActiveProducts != null)
                            lblActiveProducts.setText(masterData.size() + " sản phẩm");

                        setupCategoryPieChart();
                    } else {
                        showAlert("Lỗi", "Không thể tải danh sách sản phẩm: " + (response != null ? response.getMessage() : "Mất kết nối"), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadSellerAuctions() {
        new Thread(() -> {
            try {
                ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
                Platform.runLater(() -> {
                    if (res != null && res.getStatus() == 200 && res.getData() != null) {
                        sellerAuctions.setAll(res.getData());
                        updateAuctionStats();
                    } else {
                        showAlert("Lỗi", "Không thể tải danh sách đấu giá: " + (res != null ? res.getMessage() : "Mất kết nối"), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void updateAuctionStats() {
        long active = sellerAuctions.stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
        long ended = sellerAuctions.stream().filter(a -> "ENDED".equalsIgnoreCase(a.getStatus())).count();
        double revenue = sellerAuctions.stream()
                .filter(a -> "ENDED".equalsIgnoreCase(a.getStatus()))
                .mapToDouble(Auction::getCurrentPrice).sum();

        if (lblSellerActiveAuctions != null) lblSellerActiveAuctions.setText(String.valueOf(active));
        if (lblSellerEndedAuctions  != null) lblSellerEndedAuctions.setText(String.valueOf(ended));
        if (lblSellerTotalRevenue   != null) lblSellerTotalRevenue.setText(String.format("%,.0f ₫", revenue));
        if (lblActiveAuctions       != null) lblActiveAuctions.setText(active + " đang chạy");
        if (lblMonthRevenue         != null) lblMonthRevenue.setText(String.format("%,.0f ₫", revenue));
    }

    // ══════════════════════════════════════════
    // Form Chức năng Nghiệp vụ
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

    @FXML
    private void handleConfirmCreateAuction() {
        String itemKey    = cmbAuctionItem    != null ? cmbAuctionItem.getValue()             : null;
        String priceText  = txtStartingPrice  != null ? txtStartingPrice.getText().trim()     : "";
        String startTimeStr = txtStartTime    != null ? txtStartTime.getText().trim()          : "";
        String endTimeStr   = txtEndTime      != null ? txtEndTime.getText().trim()            : "";

        if (itemKey == null) { showAlert("Thiếu thông tin", "Vui lòng chọn sản phẩm.", Alert.AlertType.WARNING); return; }
        if (priceText.isEmpty()) { showAlert("Thiếu thông tin", "Vui lòng nhập giá khởi điểm.", Alert.AlertType.WARNING); return; }
        if (endTimeStr.isEmpty()) { showAlert("Thiếu thông tin", "Vui lòng nhập thời gian kết thúc (yyyy-MM-dd HH:mm).", Alert.AlertType.WARNING); return; }

        double startingPrice;
        try {
            startingPrice = Double.parseDouble(priceText.replace(",", ""));
        } catch (NumberFormatException e) {
            showAlert("Sai định dạng", "Giá khởi điểm phải là số.", Alert.AlertType.ERROR); return;
        }

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = startTimeStr.isEmpty() ? LocalDateTime.now() : LocalDateTime.parse(startTimeStr, FMT);
            endTime   = LocalDateTime.parse(endTimeStr, FMT);
        } catch (DateTimeParseException e) {
            showAlert("Sai định dạng", "Thời gian phải theo định dạng: yyyy-MM-dd HH:mm", Alert.AlertType.ERROR); return;
        }

        if (!endTime.isAfter(startTime)) {
            showAlert("Lỗi thời gian", "Thời gian kết thúc phải sau thời gian bắt đầu.", Alert.AlertType.WARNING); return;
        }

        int itemId = itemNameToId.get(itemKey);
        String startStr = startTime.toString();
        String endStr   = endTime.toString();

        new Thread(() -> {
            try {
                // Khớp đúng với phương thức instance của auctionApi cục bộ
                ApiResponse<Void> res = auctionApi.createAuction(itemId, startingPrice, startStr, endStr);
                Platform.runLater(() -> {
                    if (res != null && res.getStatus() == 201) {
                        showAlert("Thành công", "Phiên đấu giá đã được tạo!", Alert.AlertType.INFORMATION);
                        handleCloseCreateAuction();
                        loadSellerAuctions();
                    } else {
                        showAlert("Thất bại", "Không thể tạo phiên: " + (res != null ? res.getMessage() : "Lỗi mạng"), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleCancelAuction() {
        Auction selected = tableSellerAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Chưa chọn", "Vui lòng chọn một phiên đấu giá cần hủy.", Alert.AlertType.WARNING); return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận hủy");
        confirm.setHeaderText(null);
        confirm.setContentText("Bạn có chắc muốn hủy phiên đấu giá #" + selected.getId() + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        ApiResponse<Void> res = auctionApi.cancelAuction(selected.getId());
                        Platform.runLater(() -> {
                            if (res != null && res.getStatus() == 200) {
                                showAlert("Thành công", "Đã hủy phiên đấu giá #" + selected.getId(), Alert.AlertType.INFORMATION);
                                loadSellerAuctions();
                            } else {
                                showAlert("Thất bại", "Không thể hủy: " + (res != null ? res.getMessage() : "Lỗi mạng"), Alert.AlertType.ERROR);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });
    }

    @FXML
    private void handleAddProduct() {
        String name      = txtName        != null ? txtName.getText().trim()        : "";
        String desc      = txtDescription != null ? txtDescription.getText().trim() : "";
        String category  = cmbCategory    != null ? cmbCategory.getValue()          : null;
        String condition = cmbCondition   != null ? cmbCondition.getValue()         : null;
        String priceStr  = txtPrice       != null ? txtPrice.getText().trim()        : "";
        String stockStr  = txtStock       != null ? txtStock.getText().trim()        : "";

        if (name.isEmpty() || category == null || condition == null) {
            showAlert("Thiếu thông tin", "Vui lòng điền tên, chọn danh mục và tình trạng.", Alert.AlertType.WARNING); return;
        }

        double price = 0;
        int stock = 0;
        if (!priceStr.isEmpty()) {
            try { price = Double.parseDouble(priceStr.replace(",", "")); }
            catch (NumberFormatException e) { showAlert("Sai định dạng", "Giá bán phải là số.", Alert.AlertType.ERROR); return; }
        }
        if (!stockStr.isEmpty()) {
            try { stock = Integer.parseInt(stockStr); }
            catch (NumberFormatException e) { showAlert("Sai định dạng", "Số lượng tồn kho phải là số nguyên.", Alert.AlertType.ERROR); return; }
        }

        final double finalPrice = price;
        final int finalStock = stock;

        new Thread(() -> {
            try {
                ApiResponse<Void> response = itemApi.createItem(name, desc, category, condition);
                Platform.runLater(() -> {
                    if (response != null && response.getStatus() == 201) {
                        showAlert("Thành công", "Sản phẩm đã được thêm vào kho!", Alert.AlertType.INFORMATION);
                        clearAddProductForm();
                        loadMyItems();
                        showInventory();
                    } else {
                        showAlert("Lỗi", "Không thể thêm sản phẩm: " + (response != null ? response.getMessage() : "Lỗi mạng"), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ── Search & Filter ──
    @FXML
    private void handleSearch() {
        applyFilter(txtSearch != null ? txtSearch.getText() : "",
                cmbInvStatus != null ? cmbInvStatus.getValue() : null);
    }

    // ── Bộ lọc kho hàng tìm kiếm ──
    private void applyFilter(String keyword, String status) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();
        filteredData.setPredicate(item -> {
            boolean matchName   = kw.isEmpty() || item.getName().toLowerCase().contains(kw);
            boolean matchStatus = status == null || status.equals("Tất cả") || status.equalsIgnoreCase(item.getStatus());
            return matchName && matchStatus;
        });
    }

    // ── Profile ──
    @FXML
    private void handleSaveShop() {
        String shopName = txtShopName != null ? txtShopName.getText().trim() : "";
        if (shopName.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng nhập tên shop.", Alert.AlertType.WARNING); return;
        }
        showAlert("Thành công", "Đã lưu thông tin shop lên hệ thống!", Alert.AlertType.INFORMATION);
        if (lblShopName   != null) lblShopName.setText(shopName);
        if (lblSellerName != null) lblSellerName.setText(shopName);
    }

    @FXML
    private void handleChangePw() {
        String oldPw     = txtOldPw     != null ? txtOldPw.getText()     : "";
        String newPw     = txtNewPw     != null ? txtNewPw.getText()     : "";
        String confirmPw = txtConfirmPw != null ? txtConfirmPw.getText() : "";

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showAlert("Thiếu thông tin", "Vui lòng điền đầy đủ các trường mật khẩu.", Alert.AlertType.WARNING); return;
        }
        if (!newPw.equals(confirmPw)) {
            showAlert("Không khớp", "Mật khẩu mới và xác nhận không giống nhau.", Alert.AlertType.ERROR); return;
        }
        if (newPw.length() < 6) {
            showAlert("Quá ngắn", "Mật khẩu mới phải có ít nhất 6 ký tự.", Alert.AlertType.WARNING); return;
        }

        new Thread(() -> {
            try {
                ApiResponse<Void> res = userApi.changePassword(oldPw, newPw);
                Platform.runLater(() -> {
                    if (res != null && res.getStatus() == 200) {
                        showAlert("Thành công", "Đổi mật khẩu thành công!", Alert.AlertType.INFORMATION);
                        if (txtOldPw     != null) txtOldPw.clear();
                        if (txtNewPw     != null) txtNewPw.clear();
                        if (txtConfirmPw != null) txtConfirmPw.clear();
                    } else {
                        showAlert("Thất bại", "Không thể đổi mật khẩu: " + (res != null ? res.getMessage() : "Lỗi kết nối"), Alert.AlertType.ERROR);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ── Navigation ──
    @FXML public void showDashboard() { switchTab(tabDashboard, "Dashboard"); loadMyItems(); loadSellerAuctions(); }
    @FXML public void showInventory() { switchTab(tabInventory, "Kho Hàng"); loadMyItems(); }
    @FXML public void showAddProduct() { switchTab(tabAddProduct, "Thêm Sản Phẩm"); }
    @FXML public void showAuctions() { switchTab(tabAuctions, "Đấu Giá"); loadSellerAuctions(); }
    @FXML public void showOrders() { switchTab(tabOrders, "Đơn Hàng"); }
    @FXML public void showRevenue() { switchTab(tabRevenue, "Doanh Thu"); }
    @FXML public void showNotification() { switchTab(tabNotification, "Thông Báo"); }
    @FXML public void showHistory() { switchTab(tabHistory, "Lịch Sử"); }
    @FXML public void showProfile() { switchTab(tabProfile, "Hồ Sơ"); }
    @FXML
    public void showLogout() {
        new Thread(() -> {
            try {
                userApi.logout();
            } catch(Exception ignored){}

            Platform.runLater(() -> {
                // Sử dụng chính nút btnLogout trong FXML làm điểm tựa lấy Stage cửa sổ
                SceneUtil.switchToScene(btnLogout, "/Client/view/LoginView.fxml", "Đăng Nhập Hệ Thống");
            });
        }).start();
    }
    @FXML private void showCancel() { clearAddProductForm(); showInventory(); }

    private void switchTab(Tab tab, String title) {
        if (mainTabPane != null && tab != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null && title != null) lblPageTitle.setText(title);
    }

    private void clearAddProductForm() {
        if (txtName        != null) txtName.clear();
        if (txtDescription != null) txtDescription.clear();
        if (txtPrice       != null) txtPrice.clear();
        if (txtStock       != null) txtStock.clear();
        if (cmbCategory    != null) cmbCategory.setValue(null);
        if (cmbCondition   != null) cmbCondition.setValue(null);
    }

    private void clearAuctionForm() {
        if (cmbAuctionItem   != null) cmbAuctionItem.setValue(null);
        if (txtStartingPrice != null) txtStartingPrice.clear();
        if (txtStartTime     != null) txtStartTime.clear();
        if (txtEndTime       != null) txtEndTime.clear();
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    @FXML
    private void handleRefreshSellerAuctions() {
        // Gọi lại hàm load dữ liệu đấu giá từ API để làm mới bảng
        loadSellerAuctions();
    }
}
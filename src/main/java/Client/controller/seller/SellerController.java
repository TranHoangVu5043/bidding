package Client.controller.seller;

import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.model.user.User;
import Client.model.auction.Order;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import Client.networking.endpoints.OrderApi;
import Client.networking.endpoints.UserApi;
import Client.util.SceneUtil;
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

import java.util.List;

public class SellerController {

    // ── Inventory table columns ──
    @FXML private TableView<Item>              tableMyItems;
    @FXML private TableColumn<Item, Integer>   colId;
    @FXML private TableColumn<Item, String>    colName;
    @FXML private TableColumn<Item, String>    colCategory;
    @FXML private TableColumn<Item, String>    colStatus;
    @FXML private TableColumn<Item, Double>  colPrice;
    @FXML private TableColumn<Item, Integer> colStock;

    // ── Add-product tab fields ──
    @FXML private TextField  txtName;
    @FXML private TextArea   txtDescription;
    @FXML private TextField  txtPrice;
    @FXML private TextField  txtStock;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private ComboBox<String> cmbCondition;

    // ── Search & Filter Inventory ──
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbInvStatus;

    // ── Sidebar buttons ──
    @FXML private Button btnDashboard;
    @FXML private Button btnInventory;
    @FXML private Button btnAddProduct;
    @FXML private Button btnAuctions;
    @FXML private Button btnOrders;
    @FXML private Button btnRevenue;
    @FXML private Button btnHistory;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;

    // ── Navigation ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabInventory;
    @FXML private Tab tabAddProduct;
    @FXML private Tab tabAuctions;
    @FXML private Tab tabOrders;
    @FXML private Tab tabRevenue;
    @FXML private Tab tabHistory;
    @FXML private Tab tabProfile;
    @FXML private Label lblPageTitle;
    @FXML private Label lblShopName;
    @FXML private Label lblSellerName;

    // ── Các trường FXML phục vụ Dashboard Stats & Charts ──
    @FXML private Label lblMonthRevenue;
    @FXML private Label lblNewOrders;
    @FXML private Label lblActiveProducts;
    @FXML private Label lblActiveAuctions;
    @FXML private LineChart<String, Number> chartWeekRevenue;
    @FXML private PieChart chartCategories;
    // ── Tab Doanh thu ──
    @FXML private Label lblRevTotal;
    @FXML private Label lblRevEnded;
    @FXML private Label lblRevActive;
    @FXML private TableView<Auction> tableRevenue;
    @FXML private TableColumn<Auction, Integer> colRevId;
    @FXML private TableColumn<Auction, Integer> colRevItem;
    @FXML private TableColumn<Auction, Double>  colRevStartPrice;
    @FXML private TableColumn<Auction, Double>  colRevFinalPrice;
    @FXML private TableColumn<Auction, String>  colRevEndTime;

    // ── Bảng quản lý Đấu giá (Tab Đấu giá) ──
    @FXML private Label lblSellerActiveAuctions;
    @FXML private Label lblSellerEndedAuctions;
    @FXML private Label lblSellerTotalRevenue;
    @FXML private TableView<Auction> tableSellerAuctions;
    @FXML private TableColumn<Auction, Integer> colSAucId;
    @FXML private TableColumn<Auction, Integer> colSAucItem;
    @FXML private TableColumn<Auction, Double> colSAucStartPrice;
    @FXML private TableColumn<Auction, Double> colSAucCurrentPrice;
    @FXML private TableColumn<Auction, String> colSAucEndTime;
    @FXML private TableColumn<Auction, String> colSAucStatus;

    // ── Nút tìm kiếm & lọc trạng thái trong tab Đấu giá ──
    @FXML private TextField txtAuctionSearch;
    @FXML private ComboBox<String> cmbAuctionStatus;
    @FXML private Label lblSelectedProduct;

    // ── Hồ sơ người bán & Đổi mật khẩu ──
    @FXML private TextField     txtShopName;
    @FXML private TextField     txtSellerPhone;
    @FXML private TextArea      txtShopDesc;
    @FXML private TextField     txtSellerAddress;
    @FXML private PasswordField txtOldPw;
    @FXML private PasswordField txtNewPw;
    @FXML private PasswordField txtConfirmPw;

    // ── Inventory table action column (
    @FXML private TableColumn<Item, Void>      colActions;

    // ── Bảng Lịch sử giao dịch ──
    @FXML private TableView<Order>             tableHistory;
    @FXML private TableColumn<Order, Long>     colHisId;
    @FXML private TableColumn<Order, String>   colHisProduct;
    @FXML private TableColumn<Order, Double>   colHisAmount;
    @FXML private TableColumn<Order, String>   colHisStatus;

    // ── History tab filters ──
    @FXML private TextField    txtHistorySearch;
    @FXML private ComboBox<String> cmbHistoryType;
    @FXML private ComboBox<String> cmbHistoryDate;

    // ── Bảng Đơn hàng (Tab Đơn hàng) ──
    @FXML private TableView<Order> tableRecentOrders;
    @FXML private TableColumn<Order, String> colROId;
    @FXML private TableColumn<Order, String> colROProduct;
    @FXML private TableColumn<Order, Double> colROTotal;
    @FXML private TableColumn<Order, String> colROStatus;

    @FXML private TextField txtAuctionPrice;
    @FXML private DatePicker dpAuctionEndDate;

    // ── Instance các Api kết nối trực tiếp Backend ──
    private final ItemApi    itemApi    = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();
    private final OrderApi   orderApi   = new OrderApi();
    private final UserApi    userApi    = new UserApi();

    // ── Các danh sách dữ liệu ObservableList & FilteredList ──
    private final ObservableList<Item>    masterData     = FXCollections.observableArrayList();
    private final ObservableList<Auction> sellerAuctions = FXCollections.observableArrayList();
    private final ObservableList<Order>   recentOrders   = FXCollections.observableArrayList();

    private FilteredList<Item>    filteredData;
    private FilteredList<Auction> filteredAuctions;

    @FXML
    public void initialize() {
        // ── Cell Factories: Kho hàng (Đã sửa lỗi đưa colPrice và colStock về đúng vị trí) ──
        if (tableMyItems != null) {
            if (colId != null) colId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colName != null) colName.setCellValueFactory(new PropertyValueFactory<>("name"));
            if (colCategory != null) colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
            if (colStatus != null) colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            if (colPrice != null) colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            if (colStock != null) colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

            filteredData = new FilteredList<>(masterData, p -> true);
            tableMyItems.setItems(filteredData);
        }

        // ── Cell Factories: Bảng Đấu giá ──
        if (tableSellerAuctions != null) {
            colSAucId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colSAucItem.setCellValueFactory(new PropertyValueFactory<>("itemId"));
            colSAucStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
            colSAucCurrentPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
            colSAucEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
            colSAucStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

            filteredAuctions = new FilteredList<>(sellerAuctions, p -> true);
            tableSellerAuctions.setItems(filteredAuctions);
        }

        // ── Cell Factories: Bảng lịch sử ──
        if (tableHistory != null) {
            if (colHisId != null) colHisId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colHisProduct != null) colHisProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
            if (colHisAmount != null) colHisAmount.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            if (colHisStatus != null) colHisStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        }

        // ── Cell Factories: Bảng Doanh thu ──
        if (tableRevenue != null) {
            colRevId.setCellValueFactory(new PropertyValueFactory<>("id"));
            colRevItem.setCellValueFactory(new PropertyValueFactory<>("itemId"));
            colRevStartPrice.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));
            colRevFinalPrice.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
            colRevEndTime.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        }

        // ── Cell Factories: Bảng Đơn hàng gần đây ──
        if (tableRecentOrders != null) {
            if (colROId != null) colROId.setCellValueFactory(new PropertyValueFactory<>("id"));
            if (colROProduct != null) colROProduct.setCellValueFactory(new PropertyValueFactory<>("productName"));
            if (colROTotal != null) colROTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
            if (colROStatus != null) colROStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            tableRecentOrders.setItems(recentOrders);
        }
        if (tableMyItems != null) {
            tableMyItems.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null && lblSelectedProduct != null) {
                    lblSelectedProduct.setText("Đang chọn: " + newValue.getName());
                    lblSelectedProduct.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold; -fx-pref-width: 150;");
                }
            });
        }

        // ── Cài đặt ComboBox ──
        if (cmbCategory != null) {
            cmbCategory.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        }
        if (cmbCondition != null) {
            cmbCondition.setItems(FXCollections.observableArrayList("NEW", "USED", "REFURBISHED"));
        }
        if (cmbInvStatus != null) {
            cmbInvStatus.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "INACTIVE", "SOLD"));
            cmbInvStatus.setValue("Tất cả");
            cmbInvStatus.valueProperty().addListener((obs, oldV, newV) -> applyFilter(txtSearch != null ? txtSearch.getText() : ""));
        }
        if (cmbAuctionStatus != null) {
            cmbAuctionStatus.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "FINISHED", "UPCOMING"));
            cmbAuctionStatus.setValue("Tất cả");
            cmbAuctionStatus.valueProperty().addListener((obs, oldV, newV) -> applyAuctionFilter());
        }

        // ── Lắng nghe sự kiện tìm kiếm Realtime ──
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilter(newVal));
        }
        if (txtAuctionSearch != null) {
            txtAuctionSearch.textProperty().addListener((obs, oldVal, newVal) -> applyAuctionFilter());
        }

        // Tải dữ liệu từ mạng khi khởi chạy ứng dụng lần đầu
        populateSellerInfo();
        loadMyItems();
        loadSellerAuctions();
        loadRecentOrdersData();
        setupWeekRevenueChart();
    }

    private void populateSellerInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;
        String name = user.getUsername() != null ? user.getUsername() : "Seller";
        if (lblShopName   != null) lblShopName.setText(name);
        if (lblSellerName != null) lblSellerName.setText(name);
        if (txtShopName   != null) txtShopName.setText(name);
    }

    // ── Tải danh sách sản phẩm  ──
    private void loadMyItems() {
        new Thread(() -> {
            ApiResponse<List<Item>> response = itemApi.getMyItems();
            Platform.runLater(() -> {
                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    masterData.setAll(response.getData());
                    if (lblActiveProducts != null) {
                        lblActiveProducts.setText(masterData.size() + " sản phẩm");
                    }
                    setupCategoryPieChart();
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    SceneUtil.showAlert("Lỗi", "Không thể tải danh sách sản phẩm: " + msg);
                }
            });
        }).start();
    }
    private void loadHistory() {
        new Thread(() -> {
            ApiResponse<List<Order>> response = orderApi.getAllOrders();
            Platform.runLater(() -> {
                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    if (tableHistory != null) {
                        tableHistory.setItems(FXCollections.observableArrayList(response.getData()));
                    }
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    SceneUtil.showAlert("Lỗi", "Không thể tải lịch sử giao dịch: " + msg);
                }
            });
        }).start();
    }
    private void loadRevenue() {
        new Thread(() -> {
            try {
                ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
                Platform.runLater(() -> {
                    if (res != null && res.getStatus() == 200 && res.getData() != null) {
                        List<Auction> ended = res.getData().stream()
                                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                                .toList();
                        List<Auction> active = res.getData().stream()
                                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                                .toList();
                        double total = ended.stream().mapToDouble(Auction::getCurrentPrice).sum();

                        if (lblRevTotal  != null) lblRevTotal.setText(String.format("%,.0f ₫", total));
                        if (lblRevEnded  != null) lblRevEnded.setText(String.valueOf(ended.size()));
                        if (lblRevActive != null) lblRevActive.setText(String.valueOf(active.size()));
                        if (tableRevenue != null) tableRevenue.setItems(FXCollections.observableArrayList(ended));
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ── Tải danh sách phiên đấu giá   ──
    private void loadSellerAuctions() {
        new Thread(() -> {
            try {
                ApiResponse<List<Auction>> res = auctionApi.getAllAuctions();
                Platform.runLater(() -> {
                    if (res != null && res.getStatus() == 200 && res.getData() != null) {
                        sellerAuctions.setAll(res.getData());
                        updateAuctionStats();
                    } else {
                        String msg = res != null ? res.getMessage() : "Mất kết nối";
                        SceneUtil.showAlert("Lỗi", "Không thể tải danh sách đấu giá: " + msg);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ── Tải dữ liệu đơn hàng gần đây ──
    private void loadRecentOrdersData() {
        new Thread(() -> {
            try {
                ApiResponse<List<Order>> response = orderApi.getRecentOrders();
                Platform.runLater(() -> {
                    if (response != null && response.getStatus() == 200 && response.getData() != null) {
                        recentOrders.setAll(response.getData());
                        if (lblNewOrders != null) {
                            lblNewOrders.setText(recentOrders.size() + " đơn");
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }
    // ── Hàm xử lý Đăng lên sàn  ──
    @FXML
    private void handleQuickAuction() {
        Item selectedItem = tableMyItems.getSelectionModel().getSelectedItem();

        if (selectedItem == null) {
            SceneUtil.showAlert("Chú ý", "Vui lòng click chọn 1 sản phẩm trong bảng trước!");
            return;
        }

        try {
            // 2. Lấy dữ liệu giá và ngày từ giao diện
            double price = Double.parseDouble(txtAuctionPrice.getText());
            java.time.LocalDate endDate = dpAuctionEndDate.getValue();

            if (endDate == null) {
                SceneUtil.showAlert("Chú ý",  "Vui lòng chọn ngày kết thúc!");
                return;
            }

            String startTime = java.time.LocalDateTime.now().withNano(0).toString();
            String endTime = endDate.atTime(23, 59, 59).toString();
            ApiResponse<Void> response = auctionApi.createAuction(
                    selectedItem.getId(),
                    price,
                    startTime,
                    endTime
            );
            if (response != null && response.getStatus() == 201) {
                SceneUtil.showAlert("Thành công", "Đã đưa sản phẩm lên sàn đấu giá thành công!");

                // Dọn dẹp form
                txtAuctionPrice.clear();
                dpAuctionEndDate.setValue(null);

                // Tự động chuyển sang Tab Đấu Giá
                showAuctions();
                loadSellerAuctions();
                updateAuctionStats();

            } else {
                String errorMsg = (response != null && response.getMessage() != null)
                        ? response.getMessage()
                        : "Lỗi kết nối máy chủ!";
                SceneUtil.showAlert("Đăng thất bại", errorMsg);
            }

        } catch (NumberFormatException e) {
            SceneUtil.showAlert("Lỗi nhập liệu", "Giá khởi điểm phải là một số hợp lệ!");
        } catch (Exception e) {
            SceneUtil.showAlert("Lỗi",  "Đã xảy ra lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }
    // ── Logic tính toán số liệu thống kê phòng tránh Lost Update ──
    private void updateAuctionStats() {
        long active = sellerAuctions.stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
        long ended = sellerAuctions.stream().filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus())).count();
        double revenue = sellerAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                .mapToDouble(Auction::getCurrentPrice).sum();

        if (lblSellerActiveAuctions != null) lblSellerActiveAuctions.setText(String.valueOf(active));
        if (lblSellerEndedAuctions  != null) lblSellerEndedAuctions.setText(String.valueOf(ended));
        if (lblSellerTotalRevenue   != null) lblSellerTotalRevenue.setText(String.format("%,.0f ₫", revenue));
        if (lblActiveAuctions       != null) lblActiveAuctions.setText(active + " đang chạy");
        if (lblMonthRevenue         != null) lblMonthRevenue.setText(String.format("%,.0f ₫", revenue));
    }

    // ── Biểu đồ Doanh thu tuần  ──
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

    // ── Biểu đồ hình tròn phân loại danh mục theo dữ liệu  ──
    private void setupCategoryPieChart() {
        if (chartCategories == null) return;
        long electronics = masterData.stream().filter(i -> "ELECTRONICS".equalsIgnoreCase(i.getCategory())).count();
        long art = masterData.stream().filter(i -> "ART".equalsIgnoreCase(i.getCategory())).count();
        long vehicle = masterData.stream().filter(i -> "VEHICLE".equalsIgnoreCase(i.getCategory())).count();

        PieChart.Data slice1 = new PieChart.Data("Điện tử (" + electronics + ")", electronics);
        PieChart.Data slice2 = new PieChart.Data("Nghệ thuật (" + art + ")", art);
        PieChart.Data slice3 = new PieChart.Data("Xe cộ (" + vehicle + ")", vehicle);
        chartCategories.setData(FXCollections.observableArrayList(slice1, slice2, slice3));
    }

    // ── Thêm sản phẩm mới  ──
    @FXML
    private void handleAddProduct() {
        String name        = txtName        != null ? txtName.getText().trim()        : "";
        String description = txtDescription != null ? txtDescription.getText().trim() : "";
        String category    = cmbCategory    != null ? cmbCategory.getValue()          : null;
        String condition   = cmbCondition   != null ? cmbCondition.getValue()         : "NEW";

        double price = 0;
        int stock = 0;
        try {
            price = txtPrice != null ? Double.parseDouble(txtPrice.getText().trim()) : 0;
            stock = txtStock != null ? Integer.parseInt(txtStock.getText().trim())   : 0;
        } catch (NumberFormatException e) {
            SceneUtil.showAlert("Lỗi", "Giá và số lượng phải là số hợp lệ.");
            return;
        }

        if (name.isEmpty() || category == null) {
            SceneUtil.showAlert("Thiếu thông tin", "Vui lòng điền tên sản phẩm và chọn danh mục.");
            return;
        }

        double finalPrice = price;
        int finalStock = stock;
        new Thread(() -> {
            ApiResponse<Void> response = itemApi.createItem(name, description, category, condition, finalPrice, finalStock);
            Platform.runLater(() -> {
                if (response != null && response.getStatus() == 201) {
                    SceneUtil.showAlert("Thành công", "Sản phẩm đã được thêm vào hệ thống!");
                    clearFields();
                    loadMyItems();
                    showInventory();
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    SceneUtil.showAlert("Lỗi", "Không thể thêm sản phẩm: " + msg);
                }
            });
        }).start();
    }

    @FXML
    private void handleSearch() {
        applyFilter(txtSearch != null ? txtSearch.getText() : "");
    }

    // Bộ lọc Realtime cho Kho hàng
    private void applyFilter(String keyword) {
        String kw = keyword == null ? "" : keyword.toLowerCase().trim();
        String statusFilter = (cmbInvStatus != null && cmbInvStatus.getValue() != null) ? cmbInvStatus.getValue() : "Tất cả";

        filteredData.setPredicate(item -> {
            boolean matchesKey = kw.isEmpty() || item.getName().toLowerCase().contains(kw);
            boolean matchesStatus = statusFilter.equals("Tất cả") || statusFilter.equalsIgnoreCase(item.getStatus());
            return matchesKey && matchesStatus;
        });
    }

    // Bộ lọc Realtime cho Đấu giá
    private void applyAuctionFilter() {
        if (filteredAuctions == null) return;

        String keyword = (txtAuctionSearch != null) ? txtAuctionSearch.getText().toLowerCase().trim() : "";
        String statusFilter = (cmbAuctionStatus != null && cmbAuctionStatus.getValue() != null) ? cmbAuctionStatus.getValue() : "Tất cả";

        filteredAuctions.setPredicate(auction -> {
            boolean matchesStatus = statusFilter.equals("Tất cả") || statusFilter.equalsIgnoreCase(auction.getStatus());
            boolean matchesKey = keyword.isEmpty()
                    || String.valueOf(auction.getId()).contains(keyword)
                    || String.valueOf(auction.getItemId()).contains(keyword);

            return matchesStatus && matchesKey;
        });
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

    // ── 🔗 ĐỒNG BỘ: Sự kiện chuyển tab của Sidebar Buttons ──
    @FXML public void showDashboard()    { switchTab(tabDashboard,    "Dashboard",         btnDashboard);  loadMyItems(); loadSellerAuctions(); loadRecentOrdersData(); }
    @FXML public void showInventory()    { switchTab(tabInventory,    "Kho Hàng",          btnInventory);  loadMyItems(); }
    @FXML public void showAddProduct()   { switchTab(tabAddProduct,   "Thêm Sản Phẩm",     btnAddProduct); }
    @FXML public void showAuctions()     { switchTab(tabAuctions,     "Đấu Giá",           btnAuctions);   loadSellerAuctions(); }
    @FXML public void showOrders()       { switchTab(tabOrders,       "Đơn Hàng",          btnOrders);     loadRecentOrdersData(); }
    @FXML public void showRevenue()      { switchTab(tabRevenue,      "Doanh Thu",         btnRevenue);    loadRevenue(); }
    @FXML public void showHistory()      { switchTab(tabHistory,      "Lịch Sử Giao Dịch", btnHistory);    loadHistory(); }
    @FXML public void showProfile()      { switchTab(tabProfile,      "Hồ Sơ Người Bán",   btnProfile); }

    @FXML
    public void showLogout() {
        SessionManager.clear();
        SceneUtil.switchToScene(btnLogout, "/Client/views/LoginView.fxml", "Login");
    }

    @FXML private void handleSaveShop()    { SceneUtil.showAlert("Thành công", "Thông tin cửa hàng đã được lưu lại."); }

    // ── Tính năng Đổi mật khẩu  ──
    @FXML
    private void handleChangePw() {
        String oldPw = (txtOldPw != null) ? txtOldPw.getText() : "";
        String newPw = (txtNewPw != null) ? txtNewPw.getText() : "";
        String confirm = (txtConfirmPw != null) ? txtConfirmPw.getText() : "";

        if (oldPw.isEmpty() || newPw.isEmpty() || confirm.isEmpty()) {
            SceneUtil.showAlert("Thiếu thông tin", "Vui lòng điền đầy đủ tất cả các trường mật khẩu.");
            return;
        }
        if (!newPw.equals(confirm)) {
            SceneUtil.showAlert("Lỗi xác nhận", "Mật khẩu mới và mật khẩu xác nhận lại không trùng khớp.");
            return;
        }
        if (newPw.equals(oldPw)) {
            SceneUtil.showAlert("Lỗi mật khẩu", "Mật khẩu mới không được trùng với mật khẩu cũ hiện tại.");
            return;
        }

        new Thread(() -> {
            try {
                ApiResponse<Void> response = userApi.changePassword(oldPw, newPw);
                Platform.runLater(() -> {
                    if (response != null && response.getStatus() == 200) {
                        SceneUtil.showAlert("Thành công", "Tài khoản của bạn đã được cập nhật mật khẩu mới thành công!");
                        if (txtOldPw != null) txtOldPw.clear();
                        if (txtNewPw != null) txtNewPw.clear();
                        if (txtConfirmPw != null) txtConfirmPw.clear();
                    } else {
                        String errorMsg = (response != null) ? response.getMessage() : "Mất kết nối tới máy chủ.";
                        SceneUtil.showAlert("Đổi mật khẩu thất bại", errorMsg);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> SceneUtil.showAlert("Lỗi hệ thống", "Đã xảy ra lỗi kết nối mạng."));
            }
        }).start();
    }
    // Đổi màu nút bấm ──
    private final String NORMAL_STYLE = "-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-background-radius: 8; -fx-font-size: 13; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14;";
    private final String ACTIVE_STYLE = "-fx-background-color: #f97316; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-size: 13; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14;";

    // ── Hàm điều hướng Tab ──
    private void switchTab(Tab tab, String title, Button activeBtn) {
        if (mainTabPane != null && tab != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null && title != null) lblPageTitle.setText(title);

        highlightButton(activeBtn);
    }

    private void highlightButton(Button active) {
        // Gom các nút của Seller vào mảng
        Button[] allButtons = {
                btnDashboard, btnInventory, btnAddProduct, btnAuctions,
                btnOrders, btnRevenue, btnHistory, btnProfile
        };

        for (Button btn : allButtons) {
            if (btn != null) {
                btn.setStyle(NORMAL_STYLE);
            }
        }

        if (active != null) {
            active.setStyle(ACTIVE_STYLE);
        }
    }
}
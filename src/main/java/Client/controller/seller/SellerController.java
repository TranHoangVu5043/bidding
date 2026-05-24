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
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class SellerController {

    // ── Inventory / History card panes ──
    @FXML private FlowPane inventoryFlowPane;
    @FXML private VBox     historyVBox;
    private Item selectedItem; // tracks which item is selected for auction

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
    @FXML private Button btnHistory;
    @FXML private Button btnProfile;
    @FXML private Button btnLogout;

    // ── Navigation ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabInventory;
    @FXML private Tab tabAddProduct;
    @FXML private Tab tabAuctions;
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
    // ── Tab Đấu giá ──
    @FXML private Label     lblSellerActiveAuctions;
    @FXML private Label     lblSellerEndedAuctions;
    @FXML private Label     lblSellerTotalRevenue;
    @FXML private FlowPane  auctionFlowPane;
    @FXML private TextField txtAuctionSearch;
    @FXML private ComboBox<String> cmbAuctionStatus;
    // ── Hồ sơ người bán & Đổi mật khẩu ──
    @FXML private TextField     txtShopName;
    @FXML private TextField     txtSellerPhone;
    @FXML private TextArea      txtShopDesc;
    @FXML private TextField     txtSellerAddress;
    @FXML private PasswordField txtOldPw;
    @FXML private PasswordField txtNewPw;
    @FXML private PasswordField txtConfirmPw;

    // ── History tab filters ──
    @FXML private TextField    txtHistorySearch;
    @FXML private ComboBox<String> cmbHistoryType;
    @FXML private ComboBox<String> cmbHistoryDate;

    // ── Instance các Api kết nối trực tiếp Backend ──
    private final ItemApi    itemApi    = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();
    private final OrderApi   orderApi   = new OrderApi();
    private final UserApi    userApi    = new UserApi();

    // ── Các danh sách dữ liệu ObservableList & FilteredList ──
    private final ObservableList<Item>    masterData     = FXCollections.observableArrayList();
    private final ObservableList<Auction> sellerAuctions = FXCollections.observableArrayList();

    // Prevents stale background auction-load responses from overwriting a cancel
    private volatile int auctionLoadVersion = 0;

    @FXML
    public void initialize() {
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
                    renderInventoryCards(masterData);
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
                    renderHistoryCards(response.getData());
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    SceneUtil.showAlert("Lỗi", "Không thể tải lịch sử giao dịch: " + msg);
                }
            });
        }).start();
    }
    // ── Tải danh sách phiên đấu giá của seller ──
    private void loadSellerAuctions() {
        final int myVersion = ++auctionLoadVersion;
        new Thread(() -> {
            try {
                ApiResponse<List<Auction>> res = auctionApi.getMyAuctions();
                Platform.runLater(() -> {
                    // Discard stale responses that arrived after a cancel
                    if (myVersion != auctionLoadVersion) return;
                    if (res != null && res.getStatus() == 200 && res.getData() != null) {
                        // Strip cancelled — they live in History, not the Auction tab
                        sellerAuctions.setAll(res.getData().stream()
                                .filter(a -> !"CANCELLED".equalsIgnoreCase(a.getStatus()))
                                .toList());
                        updateAuctionStats();
                        renderAuctionCards(sellerAuctions);
                        applyFilter(txtSearch != null ? txtSearch.getText() : "");
                    } else {
                        String msg = res != null ? res.getMessage() : "Mất kết nối";
                        SceneUtil.showAlert("Lỗi", "Không thể tải danh sách đấu giá: " + msg);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
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
        List<Item> filtered = masterData.stream()
                .filter(item -> kw.isEmpty() || item.getName().toLowerCase().contains(kw))
                .filter(item -> statusFilter.equals("Tất cả") || statusFilter.equalsIgnoreCase(item.getStatus()))
                .toList();
        renderInventoryCards(filtered);
    }

    // Bộ lọc Realtime cho Đấu giá
    private void applyAuctionFilter() {
        String keyword = (txtAuctionSearch != null) ? txtAuctionSearch.getText().toLowerCase().trim() : "";
        String statusFilter = (cmbAuctionStatus != null && cmbAuctionStatus.getValue() != null) ? cmbAuctionStatus.getValue() : "Tất cả";

        List<Auction> filtered = sellerAuctions.stream()
                .filter(a -> statusFilter.equals("Tất cả") || statusFilter.equalsIgnoreCase(a.getStatus()))
                .filter(a -> keyword.isEmpty()
                        || String.valueOf(a.getId()).contains(keyword)
                        || String.valueOf(a.getItemId()).contains(keyword))
                .toList();

        renderAuctionCards(filtered);
    }

    // ── Render auction cards ──
    private void renderAuctionCards(List<Auction> auctions) {
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();

        if (auctions == null || auctions.isEmpty()) {
            Label empty = new Label("Bạn chưa có phiên đấu giá nào.");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 30;");
            auctionFlowPane.getChildren().add(empty);
            return;
        }
        for (Auction a : auctions) {
            auctionFlowPane.getChildren().add(buildAuctionCard(a));
        }
    }

    private VBox buildAuctionCard(Auction auction) {
        Item item = masterData.stream()
                .filter(i -> i.getId() == auction.getItemId())
                .findFirst().orElse(null);

        String itemName = item != null ? item.getName() : "SP #" + auction.getItemId();
        String category = item != null ? item.getCategory() : "";

        // ── Image / icon area ──
        Label iconLabel = new Label(categoryEmoji(category));
        iconLabel.setStyle("-fx-font-size: 46;");

        StackPane imageArea = new StackPane(iconLabel);
        imageArea.setPrefSize(170, 125);
        imageArea.setStyle("-fx-background-color: " + categoryGradient(category) + ";" +
                           "-fx-background-radius: 10 10 0 0;");

        // ── Price badge ──
        Label priceLabel = new Label(String.format("$ %,.0f", auction.getCurrentPrice()));
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 5 10;");

        // ── Item name ──
        Label nameLabel = new Label(itemName);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(154);
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        // ── Status badge ──
        Label statusLabel = new Label(auction.getStatus());
        statusLabel.setStyle("-fx-background-color: " + statusColor(auction.getStatus()) + ";" +
                             "-fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;" +
                             "-fx-padding: 2 7; -fx-background-radius: 4;");

        HBox statusBox = new HBox(statusLabel);
        statusBox.setPadding(new Insets(4, 0, 0, 0));

        // ── Bottom info area ──
        VBox info = new VBox(4, nameLabel, statusBox);
        info.setPadding(new Insets(8, 8, 10, 8));

        // ── Assemble card ──
        VBox card = new VBox(imageArea, priceLabel, info);
        card.setPrefWidth(170);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 3);" +
                      "-fx-cursor: hand;");
        card.setOnMouseClicked(e -> showSellerAuctionDetail(auction));
        return card;
    }

    private String categoryEmoji(String cat) {
        if (cat == null) return "📦";
        return switch (cat.toUpperCase()) {
            case "ELECTRONICS" -> "📱";
            case "ART"         -> "🎨";
            case "VEHICLE"     -> "🚗";
            default            -> "📦";
        };
    }

    private String categoryGradient(String cat) {
        if (cat == null) return "#f1f5f9, #cbd5e1";
        return switch (cat.toUpperCase()) {
            case "ELECTRONICS" -> "linear-gradient(to bottom, #dbeafe, #93c5fd)";
            case "ART"         -> "linear-gradient(to bottom, #fce7f3, #f9a8d4)";
            case "VEHICLE"     -> "linear-gradient(to bottom, #d1fae5, #6ee7b7)";
            default            -> "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
        };
    }

    private String statusColor(String status) {
        if (status == null) return "#9ca3af";
        return switch (status.toUpperCase()) {
            case "ACTIVE"    -> "#22c55e";
            case "UPCOMING"  -> "#3b82f6";
            case "FINISHED"  -> "#6b7280";
            case "CANCELLED" -> "#ef4444";
            default          -> "#9ca3af";
        };
    }

    // ── Inventory cards ──
    private void renderInventoryCards(List<Item> items) {
        if (inventoryFlowPane == null) return;
        inventoryFlowPane.getChildren().clear();
        if (items == null || items.isEmpty()) {
            Label empty = new Label("Kho hàng trống. Hãy thêm sản phẩm mới.");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 30;");
            inventoryFlowPane.getChildren().add(empty);
            return;
        }
        for (Item item : items) {
            VBox card = buildItemCard(item);
            card.setOnMouseClicked(e -> {
                selectedItem = item;
                // Highlight selected card, reset others
                inventoryFlowPane.getChildren().forEach(n -> n.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 10;" +
                    "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,3); -fx-cursor: hand;"));
                card.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 10;" +
                    "-fx-border-color: #3b82f6; -fx-border-radius: 10; -fx-border-width: 2;" +
                    "-fx-effect: dropshadow(gaussian,rgba(59,130,246,0.3),10,0,0,3); -fx-cursor: hand;");
                // Show detail popup
                showItemDetail(item);
            });
            inventoryFlowPane.getChildren().add(card);
        }
    }

    // ── Item Detail Popup (with inline auction posting) ──
    private void showItemDetail(Item item) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Chi tiết sản phẩm");
        popup.setResizable(false);

        // ── Colored header ──
        Label emoji = new Label(categoryEmoji(item.getCategory()));
        emoji.setStyle("-fx-font-size: 52;");

        Label nameLabel = new Label(item.getName() != null ? item.getName() : "—");
        nameLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(360);

        String catColor = switch (item.getCategory() == null ? "" : item.getCategory().toUpperCase()) {
            case "ELECTRONICS" -> "#3b82f6";
            case "ART"         -> "#ec4899";
            case "VEHICLE"     -> "#10b981";
            default            -> "#9ca3af";
        };
        Label catBadge = new Label(item.getCategory() != null ? item.getCategory() : "OTHER");
        catBadge.setStyle("-fx-background-color: " + catColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 3 9; -fx-background-radius: 20;");

        VBox header = new VBox(8, emoji, nameLabel, catBadge);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 24, 16, 24));
        header.setStyle("-fx-background-color: " + categoryGradient(item.getCategory()) + ";");

        // ── Detail rows ──
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 4, 28));

        addDetailRow(grid, 0, "🔧 Tình trạng",  item.getCondition());
        addDetailRow(grid, 1, "💰 Giá gốc",      String.format("%,.0f ₫", item.getPrice()));
        addDetailRow(grid, 2, "📦 Tồn kho",      String.valueOf(item.getStock()));
        addDetailRow(grid, 3, "📊 Trạng thái",   item.getStatus());

        // ── Description ──
        Label descTitle = new Label("📝 Mô tả");
        descTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 13;");

        String descText = (item.getDescription() != null && !item.getDescription().isBlank())
                ? item.getDescription() : "(Không có mô tả)";
        Label descLabel = new Label(descText);
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(364);
        descLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12; -fx-line-spacing: 2;");

        VBox descBox = new VBox(6, descTitle, descLabel);
        descBox.setPadding(new Insets(14, 28, 10, 28));

        // ── Divider ──
        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(4, 20, 4, 20));

        // ── Auction Posting Section ──
        boolean hasOngoingAuction = sellerAuctions.stream()
                .anyMatch(a -> a.getItemId() == item.getId()
                        && ("ACTIVE".equalsIgnoreCase(a.getStatus())
                            || "UPCOMING".equalsIgnoreCase(a.getStatus())));

        Label auctionHeader = new Label("🏷  Đăng lên sàn đấu giá");
        auctionHeader.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #c2410c;");

        VBox auctionBox;
        if (hasOngoingAuction) {
            // Already on auction — show a notice instead of the form
            Label notice = new Label("⚠️  Sản phẩm này đang có phiên đấu giá chưa kết thúc. "
                    + "Bạn có thể tạo phiên mới sau khi phiên hiện tại hoàn thành hoặc bị hủy.");
            notice.setWrapText(true);
            notice.setMaxWidth(364);
            notice.setStyle("-fx-text-fill: #92400e; -fx-font-size: 11; -fx-line-spacing: 2;");
            auctionBox = new VBox(8, auctionHeader, notice);
        } else {
            Label priceHint = new Label("Giá khởi điểm (₫)");
            priceHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            TextField priceField = new TextField(String.format("%.0f", item.getPrice()));
            priceField.setStyle("-fx-background-radius: 6; -fx-border-color: #D1D5DB;" +
                    " -fx-border-radius: 6; -fx-padding: 7; -fx-font-size: 12;");

            Label startHint = new Label("Ngày bắt đầu");
            startHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            DatePicker dpStart = new DatePicker(java.time.LocalDate.now());
            dpStart.setMaxWidth(Double.MAX_VALUE);

            Label endHint = new Label("Ngày kết thúc");
            endHint.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
            DatePicker dpEnd = new DatePicker();
            dpEnd.setPromptText("Chọn ngày kết thúc...");
            dpEnd.setMaxWidth(Double.MAX_VALUE);

            Button btnPost = new Button("Đăng lên sàn  →");
            btnPost.setMaxWidth(Double.MAX_VALUE);
            btnPost.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; " +
                    "-fx-padding: 10; -fx-cursor: hand;");
            btnPost.setOnAction(e -> {
                try {
                    double price = Double.parseDouble(
                            priceField.getText().trim().replace(",", "").replace(".", ""));
                    java.time.LocalDate startDate = dpStart.getValue();
                    java.time.LocalDate endDate   = dpEnd.getValue();

                    if (startDate == null) {
                        SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn ngày bắt đầu!");
                        return;
                    }
                    if (endDate == null) {
                        SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn ngày kết thúc!");
                        return;
                    }
                    if (startDate.isBefore(java.time.LocalDate.now())) {
                        SceneUtil.showAlert("Ngày không hợp lệ", "Ngày bắt đầu không được là ngày trong quá khứ!");
                        return;
                    }
                    if (!endDate.isAfter(startDate)) {
                        SceneUtil.showAlert("Ngày không hợp lệ", "Ngày kết thúc phải sau ngày bắt đầu!");
                        return;
                    }

                    String startTime = startDate.atStartOfDay().withNano(0).toString();
                    String endTime   = endDate.atTime(23, 59, 59).toString();
                    btnPost.setDisable(true);
                    btnPost.setText("Đang đăng...");
                    new Thread(() -> {
                        ApiResponse<Auction> resp = auctionApi.createAuction(
                                item.getId(), price, startTime, endTime);
                        Platform.runLater(() -> {
                            btnPost.setDisable(false);
                            btnPost.setText("Đăng lên sàn  →");
                            if (resp != null && resp.getStatus() == 201) {
                                popup.close();
                                SceneUtil.showAlert("Thành công",
                                        "\"" + item.getName() + "\" đã được đưa lên sàn đấu giá!");
                                loadSellerAuctions();
                                showAuctions();
                            } else {
                                String msg = resp != null ? resp.getMessage() : "Mất kết nối";
                                SceneUtil.showAlert("Đăng thất bại", msg);
                            }
                        });
                    }).start();
                } catch (NumberFormatException ex) {
                    SceneUtil.showAlert("Lỗi nhập liệu", "Giá khởi điểm phải là số hợp lệ!");
                }
            });
            auctionBox = new VBox(8, auctionHeader,
                    priceHint, priceField,
                    startHint, dpStart,
                    endHint, dpEnd,
                    btnPost);
        }

        auctionBox.setPadding(new Insets(12, 28, 4, 28));
        auctionBox.setStyle("-fx-background-color: #fff7ed;");

        // ── Divider 2 ──
        Separator sep2 = new Separator();
        sep2.setPadding(new Insets(4, 20, 4, 20));

        // ── Return button ──
        Button btnClose = new Button("← Quay lại");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; " +
                "-fx-padding: 9 28; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());

        HBox btnBar = new HBox(btnClose);
        btnBar.setAlignment(Pos.CENTER);
        btnBar.setPadding(new Insets(12, 20, 20, 20));

        ScrollPane scroll = new ScrollPane(
                new VBox(header, grid, descBox, sep1, auctionBox, sep2, btnBar));
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafc; -fx-background: #f8fafc;");
        scroll.setPrefHeight(620);

        popup.setScene(new Scene(scroll, 420, 620));
        popup.showAndWait();
    }

    // ── Seller Auction Detail Popup ──
    private void showSellerAuctionDetail(Auction auction) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Chi tiết phiên đấu giá #" + auction.getId());
        popup.setResizable(false);

        // Look up associated item from masterData for name / category
        Item item = masterData.stream()
                .filter(i -> i.getId() == auction.getItemId())
                .findFirst().orElse(null);
        String itemName = item != null ? item.getName() : "SP #" + auction.getItemId();
        String category = item != null ? item.getCategory() : "";

        String status    = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";
        String headerBg  = switch (status) {
            case "ACTIVE"   -> categoryGradient(category);
            case "UPCOMING" -> "linear-gradient(to bottom, #e0e7ff, #a5b4fc)";
            case "FINISHED" -> "linear-gradient(to bottom, #f1f5f9, #cbd5e1)";
            default         -> "linear-gradient(to bottom, #fee2e2, #fca5a5)";
        };
        String badgeColor = statusColor(status);
        String statusText = switch (status) {
            case "ACTIVE"   -> "● Đang diễn ra";
            case "UPCOMING" -> "● Sắp diễn ra";
            case "FINISHED" -> "✓ Đã kết thúc";
            default         -> "✕ Đã hủy";
        };

        // ── Header ──
        Label iconLabel = new Label(categoryEmoji(category));
        iconLabel.setStyle("-fx-font-size: 52;");

        Label titleLabel = new Label(itemName);
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(360);

        Label subLabel = new Label("Phiên đấu giá #" + auction.getId());
        subLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #475569;");

        Label statusBadge = new Label(statusText);
        statusBadge.setStyle("-fx-background-color: " + badgeColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 11; -fx-font-weight: bold; -fx-padding: 3 10; -fx-background-radius: 20;");

        VBox header = new VBox(6, iconLabel, titleLabel, subLabel, statusBadge);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(24, 24, 16, 24));
        header.setStyle("-fx-background-color: " + headerBg + ";");

        // ── Detail rows ──
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 28, 16, 28));

        addDetailRow(grid, 0, "💵 Giá khởi điểm", String.format("%,.0f ₫", auction.getStartingPrice()));
        addDetailRow(grid, 1, "🔥 Giá hiện tại",  String.format("%,.0f ₫", auction.getCurrentPrice()));
        addDetailRow(grid, 2, "🕐 Bắt đầu",       formatDisplayTime(auction.getStartTime()));
        addDetailRow(grid, 3, "🕔 Kết thúc",       formatDisplayTime(auction.getEndTime()));

        // ── Divider ──
        Separator sep = new Separator();
        sep.setPadding(new Insets(0, 20, 0, 20));

        // ── Action buttons ──
        VBox btnBox = new VBox(8);
        btnBox.setPadding(new Insets(14, 28, 24, 28));

        boolean canCancel = "ACTIVE".equals(status) || "UPCOMING".equals(status);
        if (canCancel) {
            Button btnCancel = new Button("✕  Hủy phiên đấu giá này");
            btnCancel.setMaxWidth(Double.MAX_VALUE);
            btnCancel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; " +
                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
            btnCancel.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận hủy");
                confirm.setHeaderText(null);
                confirm.setContentText("Bạn có chắc muốn hủy phiên đấu giá #" + auction.getId() + "?");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.OK) {
                        new Thread(() -> {
                            ApiResponse<Void> res = auctionApi.cancelAuction(auction.getId());
                            Platform.runLater(() -> {
                                popup.close();
                                if (res != null && res.getStatus() == 200) {
                                    // Invalidate any in-flight loadSellerAuctions so a stale
                                    // ACTIVE response can't restore the just-cancelled auction
                                    auctionLoadVersion++;
                                    sellerAuctions.removeIf(a -> a.getId() == auction.getId());
                                    updateAuctionStats();
                                    renderAuctionCards(sellerAuctions);
                                    applyFilter(txtSearch != null ? txtSearch.getText() : "");
                                    SceneUtil.showAlert("Thành công",
                                            "Đã hủy phiên đấu giá #" + auction.getId() + ".");
                                } else {
                                    String msg = res != null ? res.getMessage() : "Mất kết nối";
                                    SceneUtil.showAlert("Lỗi hủy", msg);
                                }
                            });
                        }).start();
                    }
                });
            });
            btnBox.getChildren().add(btnCancel);
        }

        Button btnClose = new Button("← Quay lại");
        btnClose.setMaxWidth(Double.MAX_VALUE);
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());
        btnBox.getChildren().add(btnClose);

        VBox root = new VBox(header, grid, sep, btnBox);
        root.setStyle("-fx-background-color: #f8fafc;");

        popup.setScene(new Scene(root, 420, canCancel ? 490 : 440));
        popup.showAndWait();
    }

    private String formatDisplayTime(String timeStr) {
        if (timeStr == null) return "—";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(timeStr);
            return String.format("%02d/%02d/%d  %02d:%02d",
                    dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear(),
                    dt.getHour(), dt.getMinute());
        } catch (Exception e) {
            return timeStr;
        }
    }

    private void addDetailRow(GridPane grid, int row, String labelText, String value) {
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-font-size: 12;");
        Label val = new Label(value != null ? value : "—");
        val.setStyle("-fx-text-fill: #1e293b; -fx-font-size: 12;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private VBox buildItemCard(Item item) {
        Label iconLabel = new Label(categoryEmoji(item.getCategory()));
        iconLabel.setStyle("-fx-font-size: 38;");

        StackPane imageArea = new StackPane(iconLabel);
        imageArea.setPrefSize(160, 110);
        imageArea.setStyle("-fx-background-color: " + categoryGradient(item.getCategory()) +
                           "; -fx-background-radius: 10 10 0 0;");

        Label priceLabel = new Label(String.format("$ %,.0f", item.getPrice()));
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;" +
                            "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 4 8;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(144);
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String catColor = switch (item.getCategory() == null ? "" : item.getCategory().toUpperCase()) {
            case "ELECTRONICS" -> "#3b82f6";
            case "ART" -> "#ec4899";
            case "VEHICLE" -> "#10b981";
            default -> "#9ca3af";
        };
        Label catLabel = new Label(item.getCategory() != null ? item.getCategory() : "OTHER");
        catLabel.setStyle("-fx-background-color: " + catColor + "; -fx-text-fill: white;" +
                          "-fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");

        // ── Auction status for this item ──
        long activeAuctionCount = sellerAuctions.stream()
                .filter(a -> a.getItemId() == item.getId()
                        && ("ACTIVE".equalsIgnoreCase(a.getStatus())
                            || "UPCOMING".equalsIgnoreCase(a.getStatus())))
                .count();
        boolean isOnAuction = activeAuctionCount > 0;
        boolean isActive = sellerAuctions.stream()
                .anyMatch(a -> a.getItemId() == item.getId()
                        && "ACTIVE".equalsIgnoreCase(a.getStatus()));

        Label stockLabel = new Label("Kho: " + item.getStock()
                + (isOnAuction ? "  •  " + activeAuctionCount + " đấu giá" : ""));
        stockLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #9ca3af;");

        HBox catBox = new HBox(6, catLabel);
        catBox.setPadding(new Insets(4, 0, 0, 0));

        VBox info = new VBox(3, nameLabel, catBox, stockLabel);
        info.setPadding(new Insets(7, 8, 10, 8));

        // Auction status overlay on the image area
        if (isOnAuction) {
            String overlayColor = isActive ? "#22c55e" : "#3b82f6";
            String overlayText  = isActive ? "● LIVE" : "● SOON";
            Label overlay = new Label(overlayText);
            overlay.setStyle("-fx-background-color: " + overlayColor + "; -fx-text-fill: white;" +
                    "-fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 0 0 6 0;");
            StackPane.setAlignment(overlay, javafx.geometry.Pos.TOP_LEFT);
            imageArea.getChildren().add(overlay);
        }

        VBox card = new VBox(imageArea, priceLabel, info);
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                      "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,3); -fx-cursor: hand;");
        return card;
    }

    // ── History VBox ──
    private void renderHistoryCards(List<Order> orders) {
        if (historyVBox == null) return;
        historyVBox.getChildren().clear();
        if (orders == null || orders.isEmpty()) {
            Label empty = new Label("Chưa có giao dịch nào.");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
            historyVBox.getChildren().add(empty);
            return;
        }
        for (Order o : orders) historyVBox.getChildren().add(buildOrderRow(o));
    }

    private HBox buildOrderRow(Order o) {
        Label idLabel = new Label("Đơn #" + o.getId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1e293b; -fx-pref-width: 120;");

        Label productLabel = new Label(o.getProductName() != null ? o.getProductName() : "—");
        productLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
        HBox.setHgrow(productLabel, javafx.scene.layout.Priority.ALWAYS);

        Label amtLabel = new Label(String.format("%,.0f ₫", o.getTotalAmount()));
        amtLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #16a34a; -fx-pref-width: 110;");

        String sColor = switch (o.getStatus() == null ? "" : o.getStatus().toUpperCase()) {
            case "COMPLETED", "PAID" -> "#16a34a";
            case "PENDING" -> "#f59e0b";
            case "CANCELLED" -> "#ef4444";
            default -> "#9ca3af";
        };
        Label statusLabel = new Label(o.getStatus() != null ? o.getStatus() : "—");
        statusLabel.setStyle("-fx-background-color: " + sColor + "; -fx-text-fill: white;" +
                             "-fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");

        HBox row = new HBox(12, idLabel, productLabel, amtLabel, statusLabel);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                     "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
        return row;
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
    @FXML public void showDashboard()    { switchTab(tabDashboard,    "Dashboard",         btnDashboard);  loadMyItems(); loadSellerAuctions(); }
    @FXML public void showInventory()    { switchTab(tabInventory,    "Kho Hàng",          btnInventory);  loadMyItems(); }
    @FXML public void showAddProduct()   { switchTab(tabAddProduct,   "Thêm Sản Phẩm",     btnAddProduct); }
    @FXML public void showAuctions()     { switchTab(tabAuctions,     "Đấu Giá",           btnAuctions);   loadSellerAuctions(); }
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
                btnHistory, btnProfile
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
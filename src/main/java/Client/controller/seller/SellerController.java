package Client.controller.seller;

import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import Client.networking.endpoints.UserApi;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
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
    private Item selectedItem;

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
    @FXML private Button btnRevenue;
    @FXML private Tab tabRevenue;
    @FXML private VBox finishedAuctionVBox;

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
    @FXML private VBox    pieLegend;
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
    @FXML private AreaChart<String, Number> chartRevenueArea;


    // ── Instance các Api kết nối trực tiếp Backend ──
    private final ItemApi         itemApi    = new ItemApi();
    private final AuctionApi      auctionApi = new AuctionApi();
    private final UserApi         userApi    = new UserApi();


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
        if (cmbHistoryType != null) {
            cmbHistoryType.setItems(FXCollections.observableArrayList("Tất cả", "Đặt giá", "Đơn hàng"));
            cmbHistoryType.setValue("Tất cả");
        }
        if (cmbHistoryDate != null) {
            cmbHistoryDate.setItems(FXCollections.observableArrayList("Tất cả", "Hôm nay", "7 ngày qua", "30 ngày qua"));
            cmbHistoryDate.setValue("Tất cả");
        }
        if (txtHistorySearch != null) {
            txtHistorySearch.textProperty().addListener((obs, o, n) -> loadHistory());
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
            ApiResponse<List<Auction>> response = auctionApi.getMyAuctions();
            Platform.runLater(() -> {
                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    List<Auction> finished = response.getData().stream()
                            .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                            .sorted((a, b) -> {
                                if (b.getEndTime() == null) return -1;
                                if (a.getEndTime() == null) return 1;
                                return b.getEndTime().compareTo(a.getEndTime());
                            })
                            .toList();
                    renderHistoryCards(finished);
                    renderRevenueData(finished);

                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    SceneUtil.showAlert("Lỗi", "Không thể tải lịch sử đấu giá: " + msg);
                }
            });
        }).start();
    }
    @FXML public void showRevenue() {
        switchTab(tabRevenue, "Doanh Thu", btnRevenue);
        loadHistory();
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
                        sellerAuctions.setAll(res.getData());
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
        if (lblNewOrders            != null) lblNewOrders.setText(ended + " phiên");
        if (lblMonthRevenue         != null) lblMonthRevenue.setText(String.format("%,.0f ₫", revenue));
    }

    // ── Biểu đồ Doanh thu tuần  ──
    private void setupWeekRevenueChart() {
        if (chartWeekRevenue == null) return;
        chartWeekRevenue.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu (₫)");
        series.getData().add(new XYChart.Data<>("Thứ 2", 1200000));
        series.getData().add(new XYChart.Data<>("Thứ 3", 1500000));
        series.getData().add(new XYChart.Data<>("Thứ 4", 800000));
        series.getData().add(new XYChart.Data<>("Thứ 5", 2500000));
        series.getData().add(new XYChart.Data<>("Thứ 6", 1900000));
        series.getData().add(new XYChart.Data<>("Thứ 7", 3000000));
        series.getData().add(new XYChart.Data<>("CN",    4500000));
        chartWeekRevenue.getData().add(series);
        Platform.runLater(() -> {
            for (XYChart.Data<String, Number> d : series.getData()) {
                if (d.getNode() != null) {
                    d.getNode().setStyle(
                            "-fx-background-color: #F97316, white;" +
                                    "-fx-background-insets: 0, 2;" +
                                    "-fx-background-radius: 5px;" +
                                    "-fx-padding: 5px;"
                    );
                    Tooltip tp = new Tooltip(String.format("%,.0f ₫", d.getYValue().doubleValue()));
                    tp.setStyle("-fx-font-size: 11; -fx-background-radius: 6;");
                    Tooltip.install(d.getNode(), tp);
                }
            }
        });
    }
    // ── Biểu đồ hình tròn phân loại danh mục theo dữ liệu  ──
    private static final String[][] PIE_LEGEND = {
            {"Điện tử",    "#3B82F6"},
            {"Nghệ thuật", "#EC4899"},
            {"Xe cộ",      "#10B981"},
    };

    private void setupCategoryPieChart() {
        if (chartCategories == null) return;
        long electronics = masterData.stream().filter(i -> "ELECTRONICS".equalsIgnoreCase(i.getCategory())).count();
        long art         = masterData.stream().filter(i -> "ART".equalsIgnoreCase(i.getCategory())).count();
        long vehicle     = masterData.stream().filter(i -> "VEHICLE".equalsIgnoreCase(i.getCategory())).count();
        long total       = electronics + art + vehicle;
        long[] counts    = {electronics, art, vehicle};

        PieChart.Data slice1 = new PieChart.Data("Điện tử",    electronics);
        PieChart.Data slice2 = new PieChart.Data("Nghệ thuật", art);
        PieChart.Data slice3 = new PieChart.Data("Xe cộ",      vehicle);
        chartCategories.setData(FXCollections.observableArrayList(slice1, slice2, slice3));

        Platform.runLater(() -> {
            String[] colors = {"#3B82F6", "#EC4899", "#10B981"};
            int[] idxRef = {0};
            for (PieChart.Data d : chartCategories.getData()) {
                int i = idxRef[0]++;
                if (d.getNode() == null) continue;
                final String col = colors[i];
                d.getNode().setStyle("-fx-pie-color: " + col + ";");
                double pct = total > 0 ? (d.getPieValue() / total * 100) : 0;
                Tooltip tp = new Tooltip(String.format("%s: %.0f%% (%,.0f sp)", d.getName(), pct, d.getPieValue()));
                tp.setStyle("-fx-font-size: 11; -fx-background-radius: 6;");
                Tooltip.install(d.getNode(), tp);
                d.getNode().setOnMouseEntered(e ->
                        d.getNode().setStyle("-fx-pie-color: " + col + "; -fx-opacity: 0.82; -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
                d.getNode().setOnMouseExited(e ->
                        d.getNode().setStyle("-fx-pie-color: " + col + "; -fx-opacity: 1; -fx-scale-x: 1; -fx-scale-y: 1;"));
            }

            // Custom legend
            if (pieLegend != null) {
                pieLegend.getChildren().clear();
                for (int i = 0; i < PIE_LEGEND.length; i++) {
                    String lbl   = PIE_LEGEND[i][0];
                    String color = PIE_LEGEND[i][1];
                    long cnt     = counts[i];
                    double pct   = total > 0 ? (cnt * 100.0 / total) : 0;

                    javafx.scene.shape.Rectangle dot = new javafx.scene.shape.Rectangle(10, 10);
                    dot.setArcWidth(3); dot.setArcHeight(3);
                    dot.setFill(javafx.scene.paint.Color.web(color));

                    Label lblName = new Label(lbl);
                    lblName.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");
                    HBox.setHgrow(lblName, Priority.ALWAYS);

                    Label lblVal = new Label(String.format("%,.0f sp  •  %.0f%%", cnt, pct));
                    lblVal.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

                    HBox row = new HBox(8, dot, lblName, lblVal);
                    row.setAlignment(Pos.CENTER_LEFT);
                    pieLegend.getChildren().add(row);
                }
            }
        });
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
        popup.initOwner(mainTabPane.getScene().getWindow());
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

        // ── Edit button ──
        Button btnEdit = new Button("✏ Chỉnh sửa");
        btnEdit.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 9 18; -fx-cursor: hand;");
        btnEdit.setOnAction(e -> { popup.close(); showEditItemDialog(item); });

        // ── Delete button (disabled when item is in an active/upcoming auction) ──
        Button btnDelete = new Button("🗑 Xóa");
        btnDelete.setDisable(hasOngoingAuction);
        btnDelete.setStyle("-fx-background-color: " + (hasOngoingAuction ? "#9ca3af" : "#ef4444") + "; " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-padding: 9 18; -fx-cursor: " + (hasOngoingAuction ? "default" : "hand") + ";");
        if (!hasOngoingAuction) {
            btnDelete.setOnAction(e -> {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Xác nhận xóa");
                confirm.setHeaderText(null);
                confirm.setContentText("Bạn có chắc muốn xóa sản phẩm \"" + item.getName() + "\"?\n"
                        + "Hành động này không thể hoàn tác.");
                confirm.showAndWait().ifPresent(resp -> {
                    if (resp != ButtonType.OK) return;
                    new Thread(() -> {
                        ApiResponse<Void> res = itemApi.deleteItem(item.getId());
                        Platform.runLater(() -> {
                            popup.close();
                            if (res != null && res.getStatus() == 200) {
                                masterData.removeIf(i -> i.getId() == item.getId());
                                renderInventoryCards(masterData);
                                if (lblActiveProducts != null)
                                    lblActiveProducts.setText(masterData.size() + " sản phẩm");
                                SceneUtil.showAlert("Đã xóa",
                                        "Sản phẩm \"" + item.getName() + "\" đã được xóa.");
                            } else {
                                String msg = res != null ? res.getMessage() : "Mất kết nối";
                                SceneUtil.showAlert("Xóa thất bại", msg);
                            }
                        });
                    }).start();
                });
            });
        }

        // ── Return button ──
        Button btnClose = new Button("← Quay lại");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-background-radius: 8; " +
                "-fx-padding: 9 18; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnClose.setOnAction(e -> popup.close());

        HBox btnBar = new HBox(8, btnEdit, btnDelete, btnClose);
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

    // ── Edit Item Popup ──
    private void showEditItemDialog(Item item) {
        Stage editStage = new Stage();
        editStage.initModality(Modality.APPLICATION_MODAL);
        editStage.initOwner(mainTabPane.getScene().getWindow());
        editStage.setTitle("Chỉnh sửa sản phẩm");
        editStage.setResizable(false);

        // ── Header ──
        Label titleLabel = new Label("✏  Chỉnh sửa: " + item.getName());
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(360);

        VBox header = new VBox(titleLabel);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 28, 16, 28));
        header.setStyle("-fx-background-color: " + categoryGradient(item.getCategory()) + ";");

        // ── Form fields ──
        Label lblName = new Label("Tên sản phẩm *");
        lblName.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField tfName = new TextField(item.getName() != null ? item.getName() : "");
        tfName.setStyle("-fx-background-radius: 6; -fx-border-color: #D1D5DB; " +
                "-fx-border-radius: 6; -fx-padding: 7; -fx-font-size: 12;");

        Label lblDesc = new Label("Mô tả");
        lblDesc.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        TextArea taDesc = new TextArea(item.getDescription() != null ? item.getDescription() : "");
        taDesc.setWrapText(true);
        taDesc.setPrefRowCount(3);
        taDesc.setStyle("-fx-background-radius: 6; -fx-border-color: #D1D5DB; " +
                "-fx-border-radius: 6; -fx-font-size: 12;");

        Label lblCat = new Label("Danh mục *");
        lblCat.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        ComboBox<String> cbCat = new ComboBox<>();
        cbCat.setItems(FXCollections.observableArrayList("ELECTRONICS", "ART", "VEHICLE"));
        cbCat.setValue(item.getCategory());
        cbCat.setMaxWidth(Double.MAX_VALUE);

        Label lblCond = new Label("Tình trạng *");
        lblCond.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #374151;");
        ComboBox<String> cbCond = new ComboBox<>();
        cbCond.setItems(FXCollections.observableArrayList("NEW", "USED", "REFURBISHED"));
        cbCond.setValue(item.getCondition());
        cbCond.setMaxWidth(Double.MAX_VALUE);

        VBox form = new VBox(10, lblName, tfName, lblDesc, taDesc, lblCat, cbCat, lblCond, cbCond);
        form.setPadding(new Insets(20, 28, 16, 28));

        // ── Save button ──
        Button btnSave = new Button("💾 Lưu thay đổi");
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13; -fx-background-radius: 8; " +
                "-fx-padding: 10; -fx-cursor: hand;");
        btnSave.setOnAction(e -> {
            String newName = tfName.getText().trim();
            String newDesc = taDesc.getText().trim();
            String newCat  = cbCat.getValue();
            String newCond = cbCond.getValue();

            if (newName.isEmpty()) {
                SceneUtil.showAlert("Thiếu thông tin", "Tên sản phẩm không được để trống.");
                return;
            }
            if (newCat == null) {
                SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn danh mục.");
                return;
            }
            if (newCond == null) {
                SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn tình trạng.");
                return;
            }

            btnSave.setDisable(true);
            btnSave.setText("Đang lưu...");

            new Thread(() -> {
                ApiResponse<Void> res = itemApi.updateItem(item.getId(), newName, newDesc, newCat, newCond);
                Platform.runLater(() -> {
                    btnSave.setDisable(false);
                    btnSave.setText("💾 Lưu thay đổi");
                    if (res != null && res.getStatus() == 200) {
                        // Update the in-memory model so the card reflects changes immediately
                        item.setName(newName);
                        item.setDescription(newDesc);
                        item.setCategory(newCat);
                        item.setCondition(newCond);
                        renderInventoryCards(masterData);
                        setupCategoryPieChart();
                        editStage.close();
                        SceneUtil.showAlert("Thành công", "Sản phẩm đã được cập nhật.");
                    } else {
                        String msg = res != null ? res.getMessage() : "Mất kết nối";
                        SceneUtil.showAlert("Cập nhật thất bại", msg);
                    }
                });
            }).start();
        });

        Button btnCancelEdit = new Button("Hủy");
        btnCancelEdit.setMaxWidth(Double.MAX_VALUE);
        btnCancelEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #6B7280; " +
                "-fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand; " +
                "-fx-border-color: #D1D5DB; -fx-border-radius: 8; -fx-border-width: 1;");
        btnCancelEdit.setOnAction(e -> editStage.close());

        VBox btnBox = new VBox(8, btnSave, btnCancelEdit);
        btnBox.setPadding(new Insets(4, 28, 24, 28));

        VBox root = new VBox(header, form, btnBox);
        root.setStyle("-fx-background-color: #f8fafc;");

        editStage.setScene(new Scene(root, 420, 530));
        editStage.showAndWait();
    }

    // ── Seller Auction Detail Popup ──
    private void showSellerAuctionDetail(Auction auction) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(mainTabPane.getScene().getWindow());
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

    // ── History VBox — shows seller's completed auctions ──
    private void renderHistoryCards(List<Auction> auctions) {
        if (historyVBox == null) return;
        historyVBox.getChildren().clear();
        if (auctions == null || auctions.isEmpty()) {
            Label empty = new Label("Chưa có phiên đấu giá nào kết thúc.");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
            historyVBox.getChildren().add(empty);
            return;
        }
        for (Auction a : auctions) historyVBox.getChildren().add(buildAuctionHistoryRow(a));
    }
    private void renderRevenueData(List<Auction> auctions) {
        // 1. Đổ dữ liệu vào bảng "Phiên đấu giá đã xong"
        if (finishedAuctionVBox != null) {
            finishedAuctionVBox.getChildren().clear();
            if (auctions == null || auctions.isEmpty()) {
                Label empty = new Label("Chưa có doanh thu từ phiên đấu giá nào.");
                empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
                finishedAuctionVBox.getChildren().add(empty);
            } else {
                for (Auction a : auctions) {
                    // Tái sử dụng hàm build dòng lịch sử giao dịch sẵn có của bạn
                    finishedAuctionVBox.getChildren().add(buildAuctionHistoryRow(a));
                }
            }
        }

        // 2. Tính toán và vẽ biểu đồ Doanh thu theo tháng (chartRevenueBar)
        if (chartRevenueArea != null) {
            chartRevenueArea.setAnimated(false);
            chartRevenueArea.getXAxis().setAnimated(false);
            chartRevenueArea.getYAxis().setAnimated(false);

            chartRevenueArea.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doanh thu đạt được (₫)");

            double[] monthlyRevenue = new double[12];
            for (Auction a : auctions) {
                try {
                    if (a.getEndTime() != null) {
                        String timeStr = a.getEndTime().replace(" ", "T");
                        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(timeStr);
                        monthlyRevenue[dt.getMonthValue() - 1] += a.getCurrentPrice();
                    }
                } catch (Exception e) {
                    System.err.println("Lỗi parse thời gian tại Auction ID " + a.getId() + ": " + e.getMessage());
                }
            }

            String[] monthLabels = {"T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"};
            for (int i = 0; i < 12; i++) {
                series.getData().add(new XYChart.Data<>(monthLabels[i], monthlyRevenue[i]));
            }
            chartRevenueArea.getData().add(series);

            // Style data points + tooltip
            Platform.runLater(() -> {
                for (XYChart.Data<String, Number> d : series.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle(
                                "-fx-background-color: #3B82F6, white;" +
                                        "-fx-background-insets: 0, 2;" +
                                        "-fx-background-radius: 5px;" +
                                        "-fx-padding: 4px;"
                        );
                        Tooltip tp = new Tooltip(String.format("%,.0f ₫", d.getYValue().doubleValue()));
                        tp.setStyle("-fx-font-size: 11; -fx-background-radius: 6;");
                        Tooltip.install(d.getNode(), tp);
                    }
                }
            });
        }
    }

    private HBox buildAuctionHistoryRow(Auction a) {
        Label idLabel = new Label("Phiên #" + a.getId());
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1e293b; -fx-pref-width: 100;");

        String itemDisplay = (a.getItemName() != null && !a.getItemName().isBlank())
                ? a.getItemName() : "Mặt hàng #" + a.getItemId();
        Label itemLabel = new Label(itemDisplay);
        itemLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
        HBox.setHgrow(itemLabel, javafx.scene.layout.Priority.ALWAYS);

        Label priceLabel = new Label(String.format("%,.0f ₫", a.getCurrentPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #16a34a; -fx-pref-width: 120;");

        Label endLabel = new Label(formatHistoryTime(a.getEndTime()));
        endLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280; -fx-pref-width: 130;");

        Label badge = new Label("✓ Kết thúc");
        badge.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white;" +
                "-fx-font-size: 10; -fx-font-weight: bold; -fx-padding: 3 8; -fx-background-radius: 4;");

        HBox row = new HBox(12, idLabel, itemLabel, priceLabel, endLabel, badge);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 16, 12, 16));
        row.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.06),6,0,0,2);");
        return row;
    }

    private String formatHistoryTime(String timeStr) {
        if (timeStr == null) return "—";
        try {
            java.time.LocalDateTime dt = java.time.LocalDateTime.parse(timeStr);
            return String.format("%02d/%02d/%d", dt.getDayOfMonth(), dt.getMonthValue(), dt.getYear());
        } catch (Exception e) { return timeStr; }
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
        SceneUtil.switchToScene(btnLogout, "/Client/view/Loginview.fxml", "Login");
    }

    @FXML private void handleSaveShop()    { SceneUtil.showAlert("Thành công", "Thông tin cửa hàng đã được lưu lại."); }
    private static int statusPriority(String s) {
        if (s == null) return 3;
        return switch (s.toUpperCase()) {
            case "ACTIVE"   -> 0;
            case "UPCOMING" -> 1;
            case "FINISHED" -> 2;
            default         -> 3;
        };
    }

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
                btnDashboard, btnInventory, btnAddProduct,
                btnAuctions, btnRevenue, btnHistory, btnProfile,
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
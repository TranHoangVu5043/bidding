package Client.controller.seller;

import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import Client.networking.endpoints.UserApi;
import Client.controller.seller.dialogs.EditItemDialog;
import Client.controller.seller.dialogs.ItemDetailDialog;
import Client.controller.seller.dialogs.SellerAuctionDetailDialog;
import Client.controller.seller.helpers.SellerCardBuilder;
import Client.controller.seller.helpers.SellerChartHelper;
import Client.util.DialogUtil;
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
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.paint.Color;
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
    @FXML private LineChart<String, Number>  chartWeekRevenue;
    @FXML private CategoryAxis               weekXAxis;
    @FXML private NumberAxis                 weekYAxis;
    @FXML private PieChart chartCategories;
    @FXML private AreaChart<String, Number>  chartRevenueArea;
    @FXML private CategoryAxis               revenueXAxis;
    @FXML private NumberAxis                 revenueYAxis;
    @FXML private VBox   pieLegend;
    // ── Tab Đấu giá ──
    @FXML private Label     lblSellerActiveAuctions;
    @FXML private Label     lblSellerEndedAuctions;
    @FXML private Label     lblSellerTotalRevenue;
    @FXML private FlowPane  auctionFlowPane;
    @FXML private TextField txtAuctionSearch;
    @FXML private ComboBox<String> cmbAuctionStatus;
    @FXML private Button    btnAuctionPrev;
    @FXML private Button    btnAuctionNext;
    @FXML private Label     lblAuctionPage;

    private static final int PAGE_SIZE = 21;
    private int currentAuctionPage = 0;
    private List<Auction> filteredAuctions = List.of();
    // ── Hồ sơ người bán & Đổi mật khẩu ──
    @FXML private TextField     txtShopName;
    @FXML private TextField     txtSellerPhone;
    @FXML private TextArea      txtShopDesc;
    @FXML private TextField     txtSellerAddress;
    @FXML private PasswordField txtOldPw;
    @FXML private PasswordField txtNewPw;
    @FXML private PasswordField txtConfirmPw;

    // ── History tab filters ──
    @FXML private TextField        txtHistorySearch;
    @FXML private ComboBox<String> cmbHistoryDate;
    /** Full list of FINISHED auctions — filtering is applied client-side from this cache. */
    private List<Auction> cachedHistory = List.of();


    // ── Instance các Api kết nối trực tiếp Backend ──
    private final ItemApi         itemApi    = new ItemApi();
    private final AuctionApi      auctionApi = new AuctionApi();
    private final UserApi         userApi    = new UserApi();


    // ── Các danh sách dữ liệu ObservableList & FilteredList ──
    private final ObservableList<Item>    masterData     = FXCollections.observableArrayList();
    private final ObservableList<Auction> sellerAuctions = FXCollections.observableArrayList();

    // Prevents stale background auction-load responses from overwriting a cancel
    private volatile int auctionLoadVersion = 0;

    // Lazy-initialized after FXML injection — chart nodes are null until then
    private SellerChartHelper chartHelper;

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
        if (cmbHistoryDate != null) {
            cmbHistoryDate.setItems(FXCollections.observableArrayList("Tất cả", "Hôm nay", "7 ngày qua", "30 ngày qua"));
            cmbHistoryDate.setValue("Tất cả");
            cmbHistoryDate.setOnAction(e -> applyHistoryFilter());
        }
        if (txtHistorySearch != null) {
            txtHistorySearch.textProperty().addListener((obs, o, n) -> applyHistoryFilter());
        }

        // Chart helper — must be created AFTER FXML injection so chart nodes are non-null
        chartHelper = new SellerChartHelper(
                chartWeekRevenue, weekYAxis,
                chartCategories, pieLegend,
                chartRevenueArea, revenueYAxis,
                finishedAuctionVBox,
                sellerAuctions, masterData);

        // Tải dữ liệu từ mạng khi khởi chạy ứng dụng lần đầu
        populateSellerInfo();
        loadMyItems();
        loadSellerAuctions();
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
                    updateAuctionStats();
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
                    cachedHistory = response.getData().stream()
                            .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                            .sorted((a, b) -> {
                                if (b.getEndTime() == null) return -1;
                                if (a.getEndTime() == null) return 1;
                                return b.getEndTime().compareTo(a.getEndTime());
                            })
                            .toList();
                    applyHistoryFilter();
                    renderRevenueData(cachedHistory);
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối";
                    SceneUtil.showAlert("Lỗi", "Không thể tải lịch sử đấu giá: " + msg);
                }
            });
        }).start();
    }

    private void applyHistoryFilter() {
        String kw   = txtHistorySearch != null ? txtHistorySearch.getText().trim().toLowerCase() : "";
        String date = cmbHistoryDate   != null ? cmbHistoryDate.getValue() : "Tất cả";

        java.time.LocalDateTime cutoff = switch (date == null ? "Tất cả" : date) {
            case "Hôm nay"    -> java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).toLocalDate().atStartOfDay();
            case "7 ngày qua" -> java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusDays(7);
            case "30 ngày qua"-> java.time.LocalDateTime.now(java.time.ZoneOffset.UTC).minusDays(30);
            default           -> null;
        };

        List<Auction> filtered = cachedHistory.stream().filter(a -> {
            // keyword: match auction id or item name
            boolean matchKw = kw.isEmpty()
                    || String.valueOf(a.getId()).contains(kw)
                    || (a.getItemName() != null && a.getItemName().toLowerCase().contains(kw));
            // date window
            boolean matchDate = true;
            if (cutoff != null && a.getEndTime() != null) {
                try {
                    java.time.LocalDateTime end = java.time.LocalDateTime.parse(
                            a.getEndTime().replace(" ", "T"));
                    matchDate = !end.isBefore(cutoff);
                } catch (Exception ignored) {}
            }
            return matchKw && matchDate;
        }).toList();

        renderHistoryCards(filtered);
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
                        setupWeekRevenueChart();
                        filteredAuctions = sellerAuctions;
                        currentAuctionPage = 0;
                        renderAuctionCards(filteredAuctions);
                        applyFilter(txtSearch != null ? txtSearch.getText() : "");
                    } else {
                        String msg = res != null ? res.getMessage() : "Mất kết nối";
                        SceneUtil.showAlert("Lỗi", "Không thể tải danh sách đấu giá: " + msg);
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    // ── Logic tính toán số liệu thống kê ──
    private void updateAuctionStats() {
        long active = sellerAuctions.stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
        long ended  = sellerAuctions.stream().filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus())).count();

        // Tổng doanh thu tab Auctions
        double totalRevenue = sellerAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                .mapToDouble(Auction::getCurrentPrice).sum();

        // Doanh thu tháng này
        int thisMonth = java.time.LocalDate.now().getMonthValue();
        int thisYear  = java.time.LocalDate.now().getYear();
        double monthRevenue = sellerAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()) && a.getEndTime() != null)
                .filter(a -> {
                    try {
                        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(a.getEndTime().replace(" ", "T"));
                        return dt.getMonthValue() == thisMonth && dt.getYear() == thisYear;
                    } catch (Exception e) { return false; }
                })
                .mapToDouble(Auction::getCurrentPrice).sum();

        // Doanh thu tháng trước (để tính % tăng trưởng)
        java.time.LocalDate lastMonthDate = java.time.LocalDate.now().minusMonths(1);
        int lastMonth = lastMonthDate.getMonthValue();
        int lastYear  = lastMonthDate.getYear();
        double lastMonthRevenue = sellerAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()) && a.getEndTime() != null)
                .filter(a -> {
                    try {
                        java.time.LocalDateTime dt = java.time.LocalDateTime.parse(a.getEndTime().replace(" ", "T"));
                        return dt.getMonthValue() == lastMonth && dt.getYear() == lastYear;
                    } catch (Exception e) { return false; }
                })
                .mapToDouble(Auction::getCurrentPrice).sum();

        // Phiên kết thúc hôm nay
        java.time.LocalDate today = java.time.LocalDate.now();
        long endingToday = sellerAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()) && a.getEndTime() != null)
                .filter(a -> {
                    try {
                        java.time.LocalDate end = java.time.LocalDateTime.parse(a.getEndTime().replace(" ", "T")).toLocalDate();
                        return end.equals(today);
                    } catch (Exception e) { return false; }
                })
                .count();

        // Sản phẩm sắp hết hàng (stock <= 3)
        long lowStock = masterData.stream().filter(i -> i.getStock() <= 3).count();

        // % tăng trưởng so tháng trước
        String growthText;
        if (lastMonthRevenue == 0) {
            growthText = monthRevenue > 0 ? "▲ Mới có doanh thu" : "Chưa có doanh thu";
        } else {
            double pct = (monthRevenue - lastMonthRevenue) / lastMonthRevenue * 100;
            growthText = pct >= 0
                    ? String.format("▲ %.0f%% so với tháng trước", pct)
                    : String.format("▼ %.0f%% so với tháng trước", Math.abs(pct));
        }

        if (lblSellerActiveAuctions != null) lblSellerActiveAuctions.setText(String.valueOf(active));
        if (lblSellerEndedAuctions  != null) lblSellerEndedAuctions.setText(String.valueOf(ended));
        if (lblSellerTotalRevenue   != null) lblSellerTotalRevenue.setText(String.format("%,.0f ₫", totalRevenue));
        if (lblActiveAuctions       != null) lblActiveAuctions.setText(active + " đang chạy");
        if (lblNewOrders            != null) lblNewOrders.setText(ended + " phiên");
        if (lblMonthRevenue         != null) lblMonthRevenue.setText(String.format("%,.0f ₫", monthRevenue));
    }

    // ── Charts — delegated to SellerChartHelper ──────────────────────────
    private void setupWeekRevenueChart()  { if (chartHelper != null) chartHelper.setupWeekRevenueChart(); }
    private void setupCategoryPieChart()  { if (chartHelper != null) chartHelper.setupCategoryPieChart(); }
    private void renderRevenueData(List<Auction> auctions) { if (chartHelper != null) chartHelper.renderRevenueData(auctions); }

    // ── Add product ───────────────────────────────────────────────────────
    @FXML
    private void handleAddProduct() {
        String name        = txtName        != null ? txtName.getText().trim()  : "";
        String description = txtDescription != null ? txtDescription.getText().trim() : "";
        String category    = cmbCategory    != null ? cmbCategory.getValue()   : null;
        String condition   = cmbCondition   != null ? cmbCondition.getValue()  : null;
        String priceText   = txtPrice       != null ? txtPrice.getText().trim() : "";
        String stockText   = txtStock       != null ? txtStock.getText().trim() : "";

        if (name.isEmpty())   { SceneUtil.showAlert("Thiếu thông tin", "Vui lòng nhập tên sản phẩm."); return; }
        if (category == null) { SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn danh mục.");     return; }
        if (condition == null){ SceneUtil.showAlert("Thiếu thông tin", "Vui lòng chọn tình trạng.");   return; }

        double price = 0;
        int    stock = 0;
        try { price = Double.parseDouble(priceText.replace(",", "").replace(".", "")); } catch (NumberFormatException ignored) {}
        try { stock = Integer.parseInt(stockText); } catch (NumberFormatException ignored) {}

        final double fp = price;
        final int    fs = stock;
        new Thread(() -> {
            ApiResponse<Void> resp = itemApi.createItem(name, description, category, condition, fp, fs);
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 201) {
                    SceneUtil.showAlert("Thành công", "Sản phẩm \"" + name + "\" đã được thêm vào kho!");
                    clearFields();
                    loadMyItems();
                    showInventory();
                } else {
                    SceneUtil.showAlert("Thêm thất bại", resp != null ? resp.getMessage() : "Mất kết nối");
                }
            });
        }).start();
    }

    // ── Inventory search & filter ─────────────────────────────────────────
    @FXML
    private void handleSearch() { applyFilter(txtSearch != null ? txtSearch.getText() : ""); }

    private void applyFilter(String keyword) {
        String kw  = keyword != null ? keyword.trim().toLowerCase() : "";
        String statusFilter = cmbInvStatus != null ? cmbInvStatus.getValue() : "Tất cả";

        List<Item> filtered = masterData.stream()
                .filter(i -> kw.isEmpty() || i.getName().toLowerCase().contains(kw))
                .filter(i -> "Tất cả".equals(statusFilter) || statusFilter == null
                        || statusFilter.equalsIgnoreCase(i.getStatus()))
                .toList();
        renderInventoryCards(filtered);
    }

    // ── Auction search & filter ───────────────────────────────────────────
    private void applyAuctionFilter() {
        String kw  = txtAuctionSearch != null ? txtAuctionSearch.getText().trim().toLowerCase() : "";
        String st  = cmbAuctionStatus != null ? cmbAuctionStatus.getValue() : "Tất cả";

        filteredAuctions = sellerAuctions.stream()
                .filter(a -> kw.isEmpty()
                        || String.valueOf(a.getId()).contains(kw)
                        || (a.getItemName() != null && a.getItemName().toLowerCase().contains(kw)))
                .filter(a -> "Tất cả".equals(st) || st == null || st.equalsIgnoreCase(a.getStatus()))
                .sorted((a, b) -> statusPriority(a.getStatus()) - statusPriority(b.getStatus()))
                .toList();
        currentAuctionPage = 0;
        renderAuctionCards(filteredAuctions);
    }

    // ── Auction card rendering + pagination ───────────────────────────────
    private void renderAuctionCards(List<Auction> auctions) {
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();

        int total = (int) Math.ceil((double) filteredAuctions.size() / PAGE_SIZE);
        int start = currentAuctionPage * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, filteredAuctions.size());
        List<Auction> page = filteredAuctions.isEmpty() ? List.of() : filteredAuctions.subList(start, end);

        if (page.isEmpty()) {
            Label empty = new Label("Không có phiên đấu giá nào.");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
            auctionFlowPane.getChildren().add(empty);
        } else {
            for (Auction a : page) {
                auctionFlowPane.getChildren().add(
                    SellerCardBuilder.buildAuctionCard(a, masterData, this::onAuctionCardClick));
            }
        }
        updateAuctionPagination(currentAuctionPage, total);
    }

    /** Fired when any auction card is clicked — opens the detail dialog. */
    private void onAuctionCardClick(Auction auction) {
        Item relatedItem = masterData.stream()
                .filter(i -> i.getId() == auction.getItemId())
                .findFirst().orElse(null);
        String itemName = relatedItem != null ? relatedItem.getName() : "SP #" + auction.getItemId();
        String category = relatedItem != null ? relatedItem.getCategory() : "";
        SellerAuctionDetailDialog.show(
            mainTabPane.getScene().getWindow(),
            auction, itemName, category, auctionApi,
            () -> {
                auctionLoadVersion++;
                sellerAuctions.removeIf(a -> a.getId() == auction.getId());
                updateAuctionStats();
                renderAuctionCards(sellerAuctions);
                applyFilter(txtSearch != null ? txtSearch.getText() : "");
            });
    }

    private void updateAuctionPagination(int page, int total) {
        if (lblAuctionPage != null) lblAuctionPage.setText("Trang " + (total == 0 ? 0 : page + 1) + " / " + Math.max(1, total));
        if (btnAuctionPrev != null) btnAuctionPrev.setDisable(page <= 0);
        if (btnAuctionNext != null) btnAuctionNext.setDisable(page >= total - 1);
    }

    @FXML private void handleAuctionPrevPage() {
        if (currentAuctionPage > 0) { currentAuctionPage--; renderAuctionCards(filteredAuctions); }
    }
    @FXML private void handleAuctionNextPage() {
        int total = (int) Math.ceil((double) filteredAuctions.size() / PAGE_SIZE);
        if (currentAuctionPage < total - 1) { currentAuctionPage++; renderAuctionCards(filteredAuctions); }
    }

    // ── Inventory card rendering ──────────────────────────────────────────
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
            VBox card = SellerCardBuilder.buildItemCard(item, sellerAuctions);
            card.setOnMouseClicked(e -> {
                selectedItem = item;
                inventoryFlowPane.getChildren().forEach(n -> n.setStyle(
                        "-fx-background-color: white; -fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,3); -fx-cursor: hand;"));
                card.setStyle("-fx-background-color: #eff6ff; -fx-background-radius: 10;" +
                        "-fx-border-color: #3b82f6; -fx-border-radius: 10; -fx-border-width: 2;" +
                        "-fx-effect: dropshadow(gaussian,rgba(59,130,246,0.3),10,0,0,3); -fx-cursor: hand;");
                ItemDetailDialog.show(
                    mainTabPane.getScene().getWindow(),
                    item, sellerAuctions, auctionApi, itemApi,
                    () -> { loadSellerAuctions(); showAuctions(); },
                    deletedItem -> {
                        masterData.removeIf(i -> i.getId() == deletedItem.getId());
                        renderInventoryCards(masterData);
                        if (lblActiveProducts != null) lblActiveProducts.setText(masterData.size() + " sản phẩm");
                    },
                    itemToEdit -> EditItemDialog.show(
                        mainTabPane.getScene().getWindow(), itemToEdit, itemApi,
                        () -> { renderInventoryCards(masterData); setupCategoryPieChart(); }
                    )
                );
            });
            inventoryFlowPane.getChildren().add(card);
        }
    }

    // ── History card rendering ────────────────────────────────────────────
    private void renderHistoryCards(List<Auction> auctions) {
        if (historyVBox == null) return;
        historyVBox.getChildren().clear();
        if (auctions == null || auctions.isEmpty()) {
            Label empty = new Label("Không tìm thấy phiên đấu giá phù hợp.");
            empty.setStyle("-fx-font-size: 14; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
            historyVBox.getChildren().add(empty);
            return;
        }
        for (Auction a : auctions) {
            if (chartHelper != null) historyVBox.getChildren().add(chartHelper.buildHistoryRow(a));
        }
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
        new Thread(() -> {
            userApi.logout();
            Platform.runLater(() -> {
                SceneUtil.switchToScene(btnLogout, "/Client/views/LoginView.fxml", "Đăng nhập");;
            });
        }).start();
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
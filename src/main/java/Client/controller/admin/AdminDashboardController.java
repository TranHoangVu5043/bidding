package Client.controller.admin;

import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.ItemApi;
import Client.networking.endpoints.UserApi;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import Client.util.SceneUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

public class AdminDashboardController {

    // ── Chart constants ──
    private static final String[] CHART_STATUSES = {"ACTIVE", "UPCOMING", "FINISHED", "CANCELLED"};
    private static final String[] CHART_COLORS = {"#3B82F6", "#F97316", "#10B981", "#EF4444"};
    private static final String[] CHART_VI       = {"Đang diễn ra", "Sắp diễn ra", "Đã kết thúc", "Đã hủy"};

    // ── Sidebar ──
    @FXML private Button btnHome;
    @FXML private Button btnUsers;
    @FXML private Button btnInventory;
    @FXML private Button btnAuctions;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;

    // ── Top bar ──
    @FXML private Label lblPageTitle;
    @FXML private Label welcomeLabel;

    // ── Dashboard KPI ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalSellers;
    @FXML private Label lblTotalAuctions;

    // ── Dashboard Feed & Chart ──
    @FXML private VBox   activityVBox;
    @FXML private PieChart chartPie;
    @FXML private Label  chartCenterCount;
    @FXML private VBox   chartLegend;

    // ── TabPane ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabUsers;
    @FXML private Tab tabInventory;
    @FXML private Tab tabAuctions;
    @FXML private Tab tabSettings;

    // ── Tab Users ──
    @FXML private Label                      lblUserCount;
    @FXML private Label                      lblActiveUserCount;
    @FXML private Label                      lblSellerCount;
    @FXML private Label                      lblBannedUserCount;
    @FXML private TableView<User>            tblUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Double>  colBalance;
    @FXML private TableColumn<User, String>  colCreatedAt;
    @FXML private TableColumn<User, String>  colUserStatus;
    @FXML private TextField                  txtUserSearch;
    @FXML private ComboBox<String>           cmbUserRole;

    // ── Tab Inventory ──
    @FXML private Label                      lblTotalItems;
    @FXML private Label                      lblActiveItems;
    @FXML private Label                      lblPendingItems;
    @FXML private Label                      lblRemovedItems;
    @FXML private TableView<Item>            tblItems;
    @FXML private TableColumn<Item, Integer> colItemId;
    @FXML private TableColumn<Item, String>  colItemName;
    @FXML private TableColumn<Item, String>  colItemCategory;
    @FXML private TableColumn<Item, Double>  colItemPrice;
    @FXML private TableColumn<Item, Integer> colItemStock;
    @FXML private TextField                  txtProductSearch;
    @FXML private ComboBox<String>           cmbProductCategory;

    // ── Tab Auctions ──
    @FXML private TextField        txtAuctionSearch;
    @FXML private ComboBox<String> cmbAuctionStatus;
    @FXML private FlowPane         auctionsAdminFlowPane;
    @FXML private Button           btnAuctionPrev;
    @FXML private Button           btnAuctionNext;
    @FXML private Label            lblAuctionPage;

    private static final int AUCTION_PAGE_SIZE = 21;
    private int              currentAuctionPage = 0;
    private List<Auction>    filteredAuctions   = List.of();

    // ── Tab Settings ──
    @FXML private PasswordField txtAdminOldPw;
    @FXML private PasswordField txtAdminNewPw;
    @FXML private PasswordField txtAdminConfirmPw;


    // ── Observable data ──
    private final ObservableList<User> allUsers      = FXCollections.observableArrayList();
    private final FilteredList<User>   filteredUsers = new FilteredList<>(allUsers, p -> true);

    private final ObservableList<Item> allItems      = FXCollections.observableArrayList();
    private final FilteredList<Item>   filteredItems = new FilteredList<>(allItems, p -> true);

    // Auction list
    private List<Auction> cachedAuctions = new ArrayList<>();

    // ── APIs ──
    private final UserApi    userApi    = new UserApi();
    private final ItemApi    itemApi    = new ItemApi();
    private final AuctionApi auctionApi = new AuctionApi();

    private Button[] sidebarButtons;

    // ════════════════════════════════════════════════════════
    // INITIALIZE
    // ════════════════════════════════════════════════════════
    @FXML
    private void initialize() {
        sidebarButtons = new Button[]{
                btnHome, btnUsers, btnInventory,
                btnAuctions, btnSettings
        };

        // Hiển thị tên user đang đăng nhập
        try {
            ApiResponse<User> meResp = userApi.getMe();
            if (meResp != null && meResp.getStatus() == 200 && meResp.getData() != null) {
                if (welcomeLabel != null) welcomeLabel.setText(meResp.getData().getUsername());
            }
        } catch (Exception ignored) {}

        setupUsersTab();
        setupInventoryTab();
        setupAuctionsTab();

        handleHome();
    }

    // ════════════════════════════════════════════════════════
    // SIDEBAR NAVIGATION
    // ════════════════════════════════════════════════════════
    @FXML private void handleHome() {
        setActiveTab(tabDashboard, btnHome, "🏠 Dashboard");
        loadDashboardData();
    }

    @FXML private void handleUsers() {
        setActiveTab(tabUsers, btnUsers, "👥 Quản Lý Người Dùng");
        loadAllUsers();
    }

    @FXML private void handleInventory() {
        setActiveTab(tabInventory, btnInventory, "📦 Quản Lý Sản Phẩm");
        loadAllItems();
    }

    @FXML private void handleAuctions() {
        setActiveTab(tabAuctions, btnAuctions, "🔨 Quản Lý Đấu Giá");
        loadAllAuctions();
    }

    @FXML private void handleSettings() {
        setActiveTab(tabSettings, btnSettings, "⚙ Cài Đặt Hệ Thống");
    }

    @FXML private void handleSignOut() {
        new Thread(() -> {
            userApi.logout();
            Platform.runLater(() -> {
                SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Đăng nhập");;
            });
        }).start();
    }

    private void setActiveTab(Tab tab, Button activeBtn, String title) {
        if (mainTabPane != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null) lblPageTitle.setText(title);

        for (Button b : sidebarButtons) {
            if (b == null) continue;
            b.setStyle(b.getStyle()
                    .replace("-fx-background-color: #2ecc71;", "-fx-background-color: transparent;"));
            b.setTextFill(Color.web("#CBD5E1"));
        }
        if (activeBtn != null) {
            activeBtn.setStyle(activeBtn.getStyle()
                    .replace("-fx-background-color: transparent;", "-fx-background-color: #2ecc71;"));
            activeBtn.setTextFill(Color.WHITE);
        }
    }

    // ════════════════════════════════════════════════════════
    // DASHBOARD
    // ════════════════════════════════════════════════════════
    private void loadDashboardData() {
        new Thread(() -> {
            ApiResponse<List<User>>    usersResp   = userApi.getAllUsers();
            ApiResponse<List<Auction>> auctionResp = auctionApi.getAllAuctions();

            Platform.runLater(() -> {
                // KPI: Users & Sellers
                if (usersResp != null && usersResp.getStatus() == 200 && usersResp.getData() != null) {
                    List<User> users = usersResp.getData();
                    long sellers = users.stream()
                            .filter(u -> "SELLER".equalsIgnoreCase(u.getRole()))
                            .count();
                    if (lblTotalUsers   != null) lblTotalUsers.setText(String.valueOf(users.size()));
                    if (lblTotalSellers != null) lblTotalSellers.setText(String.valueOf(sellers));
                } else {
                    System.err.println("[Dashboard/Users] lỗi: " +
                            (usersResp != null ? usersResp.getStatus() : "null"));
                }

                // KPI: Auctions + Revenue từ FINISHED auctions + Donut chart + Activity feed
                if (auctionResp != null && auctionResp.getStatus() == 200 && auctionResp.getData() != null) {
                    cachedAuctions = auctionResp.getData();

                    if (lblTotalAuctions != null)
                        lblTotalAuctions.setText(String.valueOf(cachedAuctions.size()));

                    // Revenue = tổng currentPrice của các phiên FINISHED
                    double revenue = cachedAuctions.stream()
                            .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                            .mapToDouble(Auction::getCurrentPrice)
                            .sum();
                    if (lblTotalRevenue != null) lblTotalRevenue.setText(formatCurrency(revenue));

                    buildPieChart(cachedAuctions);
                    updateActivityFeed(cachedAuctions);
                } else {
                    System.err.println("[Dashboard/Auctions] lỗi: " +
                            (auctionResp != null ? auctionResp.getStatus() : "null"));
                }
            });
        }).start();
    }

    /** Vẽ donut chart bằng PieChart của JavaFX */
    private void buildPieChart(List<Auction> auctions) {
        if (chartPie == null || auctions == null) return;

        // --- 1. Đếm số lượng theo status ---
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String s : CHART_STATUSES) counts.put(s, 0L);
        for (Auction a : auctions) {
            String st = a.getStatus() != null ? a.getStatus().toUpperCase() : "";
            counts.merge(st, 1L, Long::sum);
        }
        long total = auctions.size();

        // --- 2. Cập nhật số ở giữa ---
        if (chartCenterCount != null) chartCenterCount.setText(String.valueOf(total));

        // --- 3. Đổ dữ liệu vào PieChart ---
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (int i = 0; i < CHART_STATUSES.length; i++) {
            long cnt = counts.getOrDefault(CHART_STATUSES[i], 0L);
            if (cnt > 0) {
                pieData.add(new PieChart.Data(CHART_VI[i], cnt));
            }
        }
        chartPie.setData(pieData);

        // --- 4. Gắn màu sắc (CSS) cho các lát cắt (Slice) ---
        for (PieChart.Data data : pieData) {
            for (int i = 0; i < CHART_VI.length; i++) {
                if (data.getName().equals(CHART_VI[i])) {
                    // Cài màu nền và viền trắng để các lát cắt phân tách rõ ràng
                    data.getNode().setStyle(
                            "-fx-pie-color: " + CHART_COLORS[i] + ";" +
                                    "-fx-border-color: white;" +
                                    "-fx-border-width: 2;"
                    );
                    break;
                }
            }
        }

        // --- 5. Vẽ lại phần Legend ở bên dưới (giữ nguyên logic cũ) ---
        if (chartLegend == null) return;
        chartLegend.getChildren().clear();

        for (int i = 0; i < CHART_STATUSES.length; i++) {
            long cnt = counts.getOrDefault(CHART_STATUSES[i], 0L);
            double pct = total > 0 ? (cnt * 100.0 / total) : 0;

            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: #F8FAFC; -fx-background-radius: 8; -fx-padding: 8 12;");

            javafx.scene.shape.Rectangle dot = new javafx.scene.shape.Rectangle(12, 12);
            dot.setArcWidth(4); dot.setArcHeight(4);
            dot.setFill(Color.web(CHART_COLORS[i]));

            Label lblName = new Label(CHART_VI[i]);
            lblName.setStyle("-fx-font-size: 12; -fx-text-fill: #374151;");
            HBox.setHgrow(lblName, Priority.ALWAYS);

            Label lblCnt = new Label(String.valueOf(cnt));
            lblCnt.setStyle(
                    "-fx-font-weight: bold; -fx-font-size: 12;" +
                            "-fx-text-fill: " + CHART_COLORS[i] + ";" +
                            "-fx-background-color: " + CHART_COLORS[i] + "18;" +
                            "-fx-background-radius: 12; -fx-padding: 2 10;"
            );

            Label lblPct = new Label(String.format("%.0f%%", pct));
            lblPct.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF; -fx-min-width: 36; -fx-alignment: CENTER_RIGHT;");

            row.getChildren().addAll(dot, lblName, lblCnt, lblPct);
            chartLegend.getChildren().add(row);
        }
    }

    /**
     * Activity feed: hiển thị 5 phiên đấu giá gần nhất (ưu tiên ACTIVE trước, rồi FINISHED).
     * Thay thế hoàn toàn feed cũ dựa trên Order.
     */
    private void updateActivityFeed(List<Auction> auctions) {
        if (activityVBox == null) return;
        activityVBox.getChildren().clear();

        if (auctions == null || auctions.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setStyle("-fx-padding: 30 0;");
            Label ico = new Label("📭");
            ico.setStyle("-fx-font-size: 28;");
            Label msg = new Label("Chưa có phiên đấu giá nào");
            msg.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
            empty.getChildren().addAll(ico, msg);
            activityVBox.getChildren().add(empty);
            return;
        }

        // Sắp xếp: ACTIVE lên đầu, sau đó theo endTime giảm dần
        List<Auction> sorted = auctions.stream()
                .sorted(Comparator
                        .comparing((Auction a) -> !"ACTIVE".equalsIgnoreCase(a.getStatus()))
                        .thenComparing((a) -> a.getEndTime() == null ? "" : a.getEndTime(),
                                Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toList());

        sorted.forEach(a -> {
            String st = a.getStatus() == null ? "" : a.getStatus().toUpperCase();

            String accentColor = switch (st) {
                case "ACTIVE"               -> "#2563EB";
                case "FINISHED"             -> "#16A34A";
                case "UPCOMING"             -> "#F97316";
                case "CANCELLED"            -> "#EF4444";
                default                     -> "#6B7280";
            };
            String bgColor = switch (st) {
                case "ACTIVE"               -> "#EFF6FF";
                case "FINISHED"             -> "#F0FDF4";
                case "UPCOMING"             -> "#FFF7ED";
                case "CANCELLED"            -> "#FEF2F2";
                default                     -> "#F9FAFB";
            };
            String icon = switch (st) {
                case "ACTIVE"               -> "🔨";
                case "FINISHED"             -> "✅";
                case "UPCOMING"             -> "⏳";
                case "CANCELLED"            -> "🚫";
                default                     -> "📋";
            };

            HBox row = new HBox(0);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle(
                    "-fx-background-color: " + bgColor + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-color: " + accentColor + "33;" +
                            "-fx-border-radius: 10;" +
                            "-fx-border-width: 1;"
            );

            Region bar = new Region();
            bar.setPrefWidth(4);
            bar.setPrefHeight(54);
            bar.setStyle(
                    "-fx-background-color: " + accentColor + ";" +
                            "-fx-background-radius: 10 0 0 10;"
            );

            HBox content = new HBox(10);
            content.setAlignment(Pos.CENTER_LEFT);
            content.setPadding(new Insets(10, 14, 10, 12));
            HBox.setHgrow(content, Priority.ALWAYS);

            Label lblIcon = new Label(icon);
            lblIcon.setStyle("-fx-font-size: 20; -fx-min-width: 28;");

            VBox info = new VBox(3);
            HBox.setHgrow(info, Priority.ALWAYS);
            String itemName = a.getItemName() != null ? a.getItemName() : ("Sản phẩm #" + a.getItemId());
            Label lblName = new Label(itemName);
            lblName.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #111827;");
            Label lblSub  = new Label("Phiên đấu giá #" + a.getId()
                    + (a.getSellerName() != null ? " · " + a.getSellerName() : ""));
            lblSub.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");
            info.getChildren().addAll(lblName, lblSub);

            VBox right = new VBox(4);
            right.setAlignment(Pos.CENTER_RIGHT);
            right.setStyle("-fx-min-width: 130;");
            Label lblAmount = new Label(formatCurrency(a.getCurrentPrice()));
            lblAmount.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #111827;");
            Label lblBadge = new Label(a.getStatus() != null ? a.getStatus() : "—");
            lblBadge.setStyle(
                    "-fx-font-size: 10; -fx-font-weight: bold;" +
                            "-fx-text-fill: " + accentColor + ";" +
                            "-fx-background-color: " + accentColor + "22;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 2 8;"
            );
            right.getChildren().addAll(lblAmount, lblBadge);

            content.getChildren().addAll(lblIcon, info, right);
            row.getChildren().addAll(bar, content);
            VBox.setMargin(row, new Insets(0, 0, 6, 0));
            activityVBox.getChildren().add(row);
        });
    }

    // ════════════════════════════════════════════════════════
    // TAB USERS
    // ════════════════════════════════════════════════════════
    private void setupUsersTab() {
        if (colUserId    != null) colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colUsername  != null) colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        if (colEmail     != null) colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        if (colRole      != null) colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        if (colCreatedAt != null) colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        if (colBalance != null) {
            colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
            colBalance.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : formatCurrency(v));
                }
            });
        }

        if (colUserStatus != null) {
            colUserStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            colUserStatus.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) { setText(null); setStyle(""); return; }
                    boolean banned = "BANNED".equalsIgnoreCase(item);
                    setText(banned ? "Đã khóa" : "Hoạt động");
                    setStyle(banned
                            ? "-fx-text-fill: #C0392B; -fx-font-weight: bold;"
                            : "-fx-text-fill: #16A34A; -fx-font-weight: bold;");
                }
            });
        }

        if (cmbUserRole != null) {
            cmbUserRole.setItems(FXCollections.observableArrayList("Tất cả", "ADMIN", "SELLER", "BIDDER"));
            cmbUserRole.setValue("Tất cả");
            cmbUserRole.setOnAction(e -> applyUserFilter());
        }
        if (txtUserSearch != null)
            txtUserSearch.textProperty().addListener((obs, o, n) -> applyUserFilter());
        if (tblUsers != null)
            tblUsers.setItems(filteredUsers);
    }

    private void loadAllUsers() {
        new Thread(() -> {
            ApiResponse<List<User>> resp = userApi.getAllUsers();
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    allUsers.setAll(resp.getData());
                    applyUserFilter();
                    updateUserStatCards(resp.getData());
                } else {
                    System.err.println("[Users] API lỗi: " +
                            (resp != null ? resp.getStatus() + " - " + resp.getMessage() : "null"));
                    showAlert(Alert.AlertType.ERROR, "Không tải được danh sách người dùng.");
                }
            });
        }).start();
    }

    private void updateUserStatCards(List<User> users) {
        long total   = users.size();
        long banned  = users.stream().filter(u -> "BANNED".equalsIgnoreCase(u.getStatus())).count();
        long active  = total - banned;
        long sellers = users.stream().filter(u -> "SELLER".equalsIgnoreCase(u.getRole())).count();

        if (lblUserCount       != null) lblUserCount.setText(String.valueOf(total));
        if (lblActiveUserCount != null) lblActiveUserCount.setText(String.valueOf(active));
        if (lblSellerCount     != null) lblSellerCount.setText(String.valueOf(sellers));
        if (lblBannedUserCount != null) lblBannedUserCount.setText(String.valueOf(banned));
    }

    private void applyUserFilter() {
        String kw   = txtUserSearch != null ? txtUserSearch.getText().trim().toLowerCase() : "";
        String role = cmbUserRole   != null ? cmbUserRole.getValue() : "Tất cả";
        filteredUsers.setPredicate(u -> {
            boolean mk = kw.isEmpty()
                    || (u.getUsername() != null && u.getUsername().toLowerCase().contains(kw))
                    || (u.getEmail()    != null && u.getEmail().toLowerCase().contains(kw));
            boolean mr = role == null || "Tất cả".equals(role) || role.equalsIgnoreCase(u.getRole());
            return mk && mr;
        });
    }

    @FXML private void handleToggleBanUser() {
        if (tblUsers == null) return;
        User sel = tblUsers.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn người dùng.");
            return;
        }

        boolean isBanned = "BANNED".equalsIgnoreCase(sel.getStatus());
        String action = isBanned ? "mở khóa" : "khóa";

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn muốn " + action + " tài khoản \"" + sel.getUsername() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;
            new Thread(() -> {
                ApiResponse<Void> resp = isBanned
                        ? userApi.unbanUser(sel.getId())
                        : userApi.banUser(sel.getId());
                Platform.runLater(() -> {
                    if (resp != null && resp.getStatus() == 200) {
                        showAlert(Alert.AlertType.INFORMATION,
                                "Đã " + action + " tài khoản: " + sel.getUsername());
                        loadAllUsers();
                    } else {
                        showAlert(Alert.AlertType.ERROR,
                                "Không thể " + action + " tài khoản. " +
                                        (resp != null ? resp.getMessage() : ""));
                    }
                });
            }).start();
        });
    }

    // ════════════════════════════════════════════════════════
    // TAB INVENTORY
    // ════════════════════════════════════════════════════════
    private void setupInventoryTab() {
        if (colItemId       != null) colItemId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if (colItemName     != null) colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (colItemCategory != null) colItemCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        if (colItemStock    != null) colItemStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        if (colItemPrice != null) {
            colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
            colItemPrice.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(Double v, boolean empty) {
                    super.updateItem(v, empty);
                    setText(empty || v == null ? null : formatCurrency(v));
                }
            });
        }


        if (cmbProductCategory != null) cmbProductCategory.setOnAction(e -> applyItemFilter());
        if (txtProductSearch   != null)
            txtProductSearch.textProperty().addListener((obs, o, n) -> applyItemFilter());
        if (tblItems != null) tblItems.setItems(filteredItems);
    }

    private void loadAllItems() {
        new Thread(() -> {
            ApiResponse<List<Item>> resp = itemApi.getAllItems();
            ApiResponse<List<Auction>> auctionResp = auctionApi.getAllAuctions(); // ← load luôn ở đây

            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    List<Item> safeItems = resp.getData().stream()
                            .filter(item -> item != null)
                            .collect(Collectors.toList());
                    allItems.setAll(safeItems);

                    if (auctionResp != null && auctionResp.getStatus() == 200 && auctionResp.getData() != null) {
                        cachedAuctions = auctionResp.getData();
                    }

                    if (cmbProductCategory != null) {
                        String cur = cmbProductCategory.getValue();
                        List<String> cats = new ArrayList<>(List.of("Tất cả"));
                        safeItems.stream()
                                .map(Item::getCategory)
                                .filter(Objects::nonNull)
                                .distinct().sorted()
                                .forEach(cats::add);
                        cmbProductCategory.setItems(FXCollections.observableArrayList(cats));
                        cmbProductCategory.setValue(cats.contains(cur) ? cur : "Tất cả");
                    }
                    applyItemFilter();
                    updateItemStatCards(safeItems);
                } else {
                    System.err.println("[Inventory] API lỗi: " +
                            (resp != null ? resp.getStatus() + " - " + resp.getMessage() : "null"));
                    showAlert(Alert.AlertType.ERROR, "Không tải được danh sách sản phẩm.");
                }
            });
        }).start();
    }

    private void updateItemStatCards(List<Item> items) {
        long total = items.size();
        Set<Integer> activeAuctionItemIds = cachedAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .map(Auction::getItemId)
                .collect(Collectors.toSet());
        Set<Integer> soldAuctionItemIds = cachedAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                .map(Auction::getItemId)
                .collect(Collectors.toSet());

        long active   = items.stream().filter(i -> activeAuctionItemIds.contains(i.getId())).count();
        long sold     = items.stream().filter(i -> soldAuctionItemIds.contains(i.getId())).count();
        long inactive = items.stream().filter(i -> "INACTIVE".equalsIgnoreCase(i.getStatus())).count();

        if (lblTotalItems   != null) lblTotalItems.setText(String.valueOf(total));
        if (lblActiveItems  != null) lblActiveItems.setText(String.valueOf(active));
        if (lblPendingItems != null) lblPendingItems.setText(String.valueOf(sold));
        if (lblRemovedItems != null) lblRemovedItems.setText(String.valueOf(inactive));
    }

    private void applyItemFilter() {
        String kw  = txtProductSearch   != null ? txtProductSearch.getText().trim().toLowerCase() : "";
        String cat = cmbProductCategory != null ? cmbProductCategory.getValue() : "Tất cả";
        filteredItems.setPredicate(item -> {
            boolean mk = kw.isEmpty()
                    || (item.getName()     != null && item.getName().toLowerCase().contains(kw))
                    || (item.getCategory() != null && item.getCategory().toLowerCase().contains(kw));
            boolean mc = cat == null || "Tất cả".equals(cat) || cat.equalsIgnoreCase(item.getCategory());
            return mk && mc;
        });
    }

    @FXML private void handleCancelProduct() {
        if (tblItems == null) return;
        Item sel = tblItems.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng chọn sản phẩm cần xóa.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa sản phẩm \"" + sel.getName() + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;
            new Thread(() -> {
                ApiResponse<Void> resp = itemApi.deleteItem(sel.getId());
                Platform.runLater(() -> {
                    if (resp != null && resp.getStatus() == 200) {
                        showAlert(Alert.AlertType.INFORMATION, "Đã xóa sản phẩm.");
                        loadAllItems();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Không thể xóa sản phẩm. " +
                                (resp != null ? resp.getMessage() : ""));
                    }
                });
            }).start();
        });
    }

    // ════════════════════════════════════════════════════════
    // TAB AUCTIONS
    // ════════════════════════════════════════════════════════
    private void setupAuctionsTab() {
        if (cmbAuctionStatus != null) {
            cmbAuctionStatus.setItems(FXCollections.observableArrayList(
                    "Tất cả", "ACTIVE", "UPCOMING", "FINISHED", "CANCELLED"));
            cmbAuctionStatus.setValue("Tất cả");
            cmbAuctionStatus.setOnAction(e -> renderAuctionCards());
        }
        if (txtAuctionSearch != null)
            txtAuctionSearch.textProperty().addListener((obs, o, n) -> renderAuctionCards());
    }

    private void loadAllAuctions() {
        new Thread(() -> {
            ApiResponse<List<Auction>> resp = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    cachedAuctions = resp.getData();
                    renderAuctionCards();
                }
            });
        }).start();
    }

    private void renderAuctionCards() {
        if (auctionsAdminFlowPane == null) return;
        String kw = txtAuctionSearch  != null ? txtAuctionSearch.getText().trim().toLowerCase() : "";
        String st = cmbAuctionStatus  != null ? cmbAuctionStatus.getValue() : "Tất cả";

        filteredAuctions = cachedAuctions.stream().filter(a -> {
            String itemName   = a.getItemName()   != null ? a.getItemName().toLowerCase()   : "";
            String sellerName = a.getSellerName() != null ? a.getSellerName().toLowerCase() : "";
            boolean mk = kw.isEmpty() || itemName.contains(kw) || sellerName.contains(kw);
            boolean ms = st == null || "Tất cả".equals(st) || st.equalsIgnoreCase(a.getStatus());
            return mk && ms;
        }).sorted((a, b) -> Integer.compare(statusRank(a.getStatus()), statusRank(b.getStatus())))
          .collect(Collectors.toList());

        currentAuctionPage = 0;
        renderAuctionPage();
    }

    private void renderAuctionPage() {
        if (auctionsAdminFlowPane == null) return;
        auctionsAdminFlowPane.getChildren().clear();

        if (filteredAuctions.isEmpty()) {
            Label empty = new Label("Không có phiên đấu giá nào.");
            empty.setStyle("-fx-font-size: 13; -fx-text-fill: #9CA3AF; -fx-padding: 20;");
            auctionsAdminFlowPane.getChildren().add(empty);
            updateAuctionPagination(0, 0);
            return;
        }

        int totalPages = (int) Math.ceil((double) filteredAuctions.size() / AUCTION_PAGE_SIZE);
        if (currentAuctionPage >= totalPages) currentAuctionPage = totalPages - 1;
        if (currentAuctionPage < 0)          currentAuctionPage = 0;

        int from = currentAuctionPage * AUCTION_PAGE_SIZE;
        int to   = Math.min(from + AUCTION_PAGE_SIZE, filteredAuctions.size());
        filteredAuctions.subList(from, to)
                .forEach(a -> auctionsAdminFlowPane.getChildren().add(buildAuctionCard(a)));

        updateAuctionPagination(currentAuctionPage, totalPages);
    }

    private void updateAuctionPagination(int page, int total) {
        if (lblAuctionPage != null)
            lblAuctionPage.setText(total == 0 ? "—" : "Trang " + (page + 1) + " / " + total);
        if (btnAuctionPrev != null) btnAuctionPrev.setDisable(page <= 0);
        if (btnAuctionNext != null) btnAuctionNext.setDisable(total == 0 || page >= total - 1);
    }

    @FXML private void handleAuctionPrevPage() {
        if (currentAuctionPage > 0) { currentAuctionPage--; renderAuctionPage(); }
    }

    @FXML private void handleAuctionNextPage() {
        int total = (int) Math.ceil((double) filteredAuctions.size() / AUCTION_PAGE_SIZE);
        if (currentAuctionPage < total - 1) { currentAuctionPage++; renderAuctionPage(); }
    }

    private int statusRank(String status) {
        if (status == null) return 99;
        return switch (status.toUpperCase()) {
            case "ACTIVE"   -> 0;
            case "UPCOMING" -> 1;
            case "FINISHED" -> 2;
            default         -> 3;
        };
    }

    private VBox buildAuctionCard(Auction a) {
        String itemName = a.getItemName()   != null ? a.getItemName()   : ("Sản phẩm #" + a.getItemId());
        String seller   = a.getSellerName() != null ? a.getSellerName() : ("Người bán #" + a.getOwnerId());
        String status   = a.getStatus()     != null ? a.getStatus()     : "—";
        boolean active  = "ACTIVE".equalsIgnoreCase(status);

        VBox card = new VBox(8);
        card.setPrefWidth(260);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.08),8,0,0,2);");

        Label lblTitle = new Label(itemName);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1F2937; -fx-wrap-text: true;");
        lblTitle.setMaxWidth(228);

        Label lblSeller = new Label("Người bán: " + seller);
        lblSeller.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        Label lblPrice = new Label("Giá hiện tại: " + formatCurrency(a.getCurrentPrice()));
        lblPrice.setStyle("-fx-font-size: 12; -fx-text-fill: #0066CC;");

        Label lblStatus = new Label(status);
        lblStatus.setStyle(active
                ? "-fx-text-fill: #16A34A; -fx-font-weight: bold; -fx-font-size: 12;"
                : "-fx-text-fill: #9CA3AF; -fx-font-size: 12;");

        Button btnCancel = new Button("Hủy phiên");
        btnCancel.setDisable(!active);
        btnCancel.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; " +
                "-fx-background-radius: 8; -fx-padding: 6 14; -fx-cursor: hand; -fx-font-size: 12;");
        btnCancel.setOnAction(e -> handleCancelAuction(a.getId(), itemName));

        card.getChildren().addAll(lblTitle, lblSeller, lblPrice, lblStatus, btnCancel);
        return card;
    }

    private void handleCancelAuction(int auctionId, String auctionTitle) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Hủy phiên \"" + auctionTitle + "\"?", ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(btn -> {
            if (btn != ButtonType.YES) return;
            new Thread(() -> {
                ApiResponse<Void> resp = auctionApi.cancelAuction(auctionId);
                Platform.runLater(() -> {
                    if (resp != null && resp.getStatus() == 200) {
                        showAlert(Alert.AlertType.INFORMATION, "Đã hủy phiên đấu giá.");
                        loadAllAuctions();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Không thể hủy phiên. " +
                                (resp != null ? resp.getMessage() : ""));
                    }
                });
            }).start();
        });
    }

    // ════════════════════════════════════════════════════════
    // TAB SETTINGS
    // ════════════════════════════════════════════════════════
    @FXML private void handleChangeAdminPw() {
        String oldPw = txtAdminOldPw     != null ? txtAdminOldPw.getText()     : "";
        String newPw = txtAdminNewPw     != null ? txtAdminNewPw.getText()     : "";
        String conPw = txtAdminConfirmPw != null ? txtAdminConfirmPw.getText() : "";

        if (oldPw.isEmpty() || newPw.isEmpty() || conPw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng điền đầy đủ các trường."); return;
        }
        if (!newPw.equals(conPw)) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu mới không khớp."); return;
        }
        if (newPw.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu mới phải ít nhất 6 ký tự."); return;
        }

        new Thread(() -> {
            ApiResponse<Void> resp = userApi.changePassword(oldPw, newPw);
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200) {
                    showAlert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công.");
                    if (txtAdminOldPw     != null) txtAdminOldPw.clear();
                    if (txtAdminNewPw     != null) txtAdminNewPw.clear();
                    if (txtAdminConfirmPw != null) txtAdminConfirmPw.clear();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Đổi mật khẩu thất bại. Kiểm tra lại mật khẩu cũ.");
                }
            });
        }).start();
    }

    // ════════════════════════════════════════════════════════
    // HELPERS
    // ════════════════════════════════════════════════════════
    private String formatCurrency(double amount) {
        return NumberFormat.getInstance(new Locale("vi", "VN")).format((long) amount) + " ₫";
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
package Client.controller.admin;

import Client.model.auction.Auction;
import Client.model.auction.Order;
import Client.model.item.Item;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.OrderApi;
import Client.networking.endpoints.UserApi;
import Client.networking.endpoints.ItemApi;
import Client.util.SceneUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

public class AdminDashboardController {

    // ── Sidebar buttons ──
    @FXML private Button btnHome;
    @FXML private Button btnUsers;
    @FXML private Button btnSellers;
    @FXML private Button btnInventory;
    @FXML private Button btnOrders;
    @FXML private Button btnAuctions;
    @FXML private Button btnAnalytics;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;

    // ── Dashboard KPI Labels ──
    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalSellers;
    @FXML private Label lblTotalAuctions;
    @FXML private Label lblPageTitle;
    @FXML private Label welcomeLabel;

    // ── Chart & Feed ──
    @FXML private PieChart chartSellers;
    @FXML private VBox activityVBox;

    // ── TabPane ──
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabUsers;
    @FXML private Tab tabSellers;
    @FXML private Tab tabInventory;
    @FXML private Tab tabOrders;
    @FXML private Tab tabAuctions;
    @FXML private Tab tabAnalytics;
    @FXML private Tab tabSettings;

    // ── Tab Users ──
    @FXML private TableView<User>            tblUsers;
    @FXML private TableColumn<User, Integer> colUserId;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, Double>  colBalance;
    @FXML private TableColumn<User, String>  colCreatedAt;
    @FXML private TextField                  txtUserSearch;
    @FXML private ComboBox<String>           cmbUserRole;

    private final ObservableList<User> allUsers = FXCollections.observableArrayList();
    private final FilteredList<User> filteredUsers = new FilteredList<>(allUsers, p -> true);

    // Tab Sellers
    @FXML private TableView<User>            tblSellers;
    @FXML private TableColumn<User, Integer> colSellerId;
    @FXML private TableColumn<User, String>  colSellerUsername;
    @FXML private TableColumn<User, String>  colSellerEmail;
    @FXML private TableColumn<User, String>  colSellerStoreName;
    @FXML private TableColumn<User, Double>  colSellerBalance;
    @FXML private TableColumn<User, String>  colSellerCreatedAt;
    @FXML private TextField                  txtSellerSearch;

    private final ObservableList<User> allSellers = FXCollections.observableArrayList();
    private final FilteredList<User> filteredSellers = new FilteredList<>(allSellers, p -> true);

    // Tab Inventory (Items)
    @FXML private TableView<Item>            tblItems;
    @FXML private TableColumn<Item, Integer> colItemId;
    @FXML private TableColumn<Item, String>  colItemName;
    @FXML private TableColumn<Item, String>  colItemCategory;
    @FXML private TableColumn<Item, Double>  colItemPrice;
    @FXML private TableColumn<Item, Integer> colItemStock;
    @FXML private TableColumn<Item, String>  colItemStatus;
    @FXML private TextField txtProductSearch;
    @FXML private ComboBox<String> cmbProductCategory;
    @FXML private ComboBox<String> cmbProductStatus;

    private final ObservableList<Item> allItems = FXCollections.observableArrayList();
    private final FilteredList<Item> filteredItems = new FilteredList<>(allItems, p -> true);

    // Tab Orders
    @FXML private TableView<Order>            tblOrders;
    @FXML private TableColumn<Order, Integer> colOrderId;
    @FXML private TableColumn<Order, String>  colOrderName;
    @FXML private TableColumn<Order, Double>  colOrderTotal;
    @FXML private TableColumn<Order, String>  colOrderStatus;
    @FXML private TextField txtOrderSearch;
    @FXML private ComboBox<String> cmbOrderStatus;

    private final ObservableList<Order> allOrders = FXCollections.observableArrayList();
    private final FilteredList<Order> filteredOrders = new FilteredList<>(allOrders, p -> true);

    // Tab Auctions
    @FXML private TextField txtAuctionSearch;
    @FXML private ComboBox<String> cmbAuctionStatus;
    @FXML private FlowPane auctionsAdminFlowPane;

    private final ObservableList<Auction> allAuctions = FXCollections.observableArrayList();
    private final FilteredList<Auction> filteredAuctions = new FilteredList<>(allAuctions, p -> true);

    //Tab Analytics
    @FXML private ComboBox<String> cmbAnalyticsPeriod;
    @FXML private BarChart<String, Number> chartMonthlyRevenue;
    @FXML private VBox topSellersVBox;

    //Tab Settings
    @FXML private PasswordField txtAdminOldPw;
    @FXML private PasswordField txtAdminNewPw;
    @FXML private PasswordField txtAdminConfirmPw;

    private Button[] sidebarButtons;

    //API clients
    private final AuctionApi auctionApi = new AuctionApi();
    private final OrderApi   orderApi   = new OrderApi();
    private final UserApi    userApi    = new UserApi();
    private final ItemApi    itemApi    = new ItemApi();


    //INITIALIZE


    @FXML
    public void initialize() {
        sidebarButtons = new Button[]{
                btnHome, btnUsers, btnSellers, btnInventory,
                btnOrders, btnAuctions, btnAnalytics, btnSettings
        };

        highlightButton(btnHome);
        lblPageTitle.setText("Dashboard");
        populateAdminInfo();
        loadDashboardData();

        setupUsersTab();
        setupSellersTab();
        setupInventoryTab();
        setupOrdersTab();
        setupAuctionsTab();
        setupAnalyticsTab();
    }

    //USERS TAB

    private void setupUsersTab() {
        if (tblUsers == null) return;

        colUserId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colBalance.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        colRole.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                String color = switch (item.toUpperCase()) {
                    case "ADMIN"  -> "#8B5CF6";
                    case "SELLER" -> "#f97316";
                    default       -> "#0066CC";
                };
                setText(item);
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }
        });

        if (cmbUserRole != null) {
            cmbUserRole.setItems(FXCollections.observableArrayList("Tất cả", "ADMIN", "SELLER", "BIDDER"));
            cmbUserRole.setValue("Tất cả");
        }

        tblUsers.setItems(filteredUsers);
        loadAllUsers();

        if (txtUserSearch != null) txtUserSearch.textProperty().addListener((o, old, val) -> applyUserFilter());
        if (cmbUserRole != null) cmbUserRole.valueProperty().addListener((o, old, val) -> applyUserFilter());
    }

    private void loadAllUsers() {
        new Thread(() -> {
            ApiResponse<List<User>> resp = userApi.getAllUsers();
            Platform.runLater(() -> {
                if (resp.getStatus() == 200 && resp.getData() != null) {
                    allUsers.setAll(resp.getData());
                }
            });
        }).start();
    }

    private void applyUserFilter() {
        String keyword = txtUserSearch != null ? txtUserSearch.getText().toLowerCase().trim() : "";
        String role = cmbUserRole != null ? cmbUserRole.getValue() : "Tất cả";
        filteredUsers.setPredicate(user -> {
            boolean matchSearch = keyword.isEmpty()
                    || (user.getUsername() != null && user.getUsername().toLowerCase().contains(keyword))
                    || (user.getEmail()    != null && user.getEmail().toLowerCase().contains(keyword));
            boolean matchRole = role == null || role.equals("Tất cả")
                    || role.equalsIgnoreCase(user.getRole());

            return matchSearch && matchRole;
        });
    }


    //SELLERS TAB


    private void setupSellersTab() {
        if (tblSellers == null) return;

        colSellerId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSellerUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colSellerEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colSellerStoreName.setCellValueFactory(new PropertyValueFactory<>("storeName"));
        colSellerBalance.setCellValueFactory(new PropertyValueFactory<>("balance"));
        colSellerCreatedAt.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        colSellerBalance.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        tblSellers.setItems(filteredSellers);
        loadAllSellers();

        if (txtSellerSearch != null) {
            txtSellerSearch.textProperty().addListener((obs, oldVal, newVal) -> applySellerFilter());
        }
    }

    private void loadAllSellers() {
        new Thread(() -> {
            ApiResponse<List<User>> resp = userApi.getAllUsers();
            Platform.runLater(() -> {
                if (resp.getStatus() == 200 && resp.getData() != null) {
                    List<User> sellers = resp.getData().stream()
                            .filter(u -> "SELLER".equalsIgnoreCase(u.getRole()))
                            .toList();
                    allSellers.setAll(sellers);
                }
            });
        }).start();
    }

    private void applySellerFilter() {
        String keyword = txtSellerSearch != null ? txtSellerSearch.getText().toLowerCase().trim() : "";
        filteredSellers.setPredicate(seller -> {
            if (keyword.isEmpty()) return true;
            return (seller.getUsername()  != null && seller.getUsername().toLowerCase().contains(keyword))
                    || (seller.getEmail()     != null && seller.getEmail().toLowerCase().contains(keyword))
                    || (seller.getStoreName() != null && seller.getStoreName().toLowerCase().contains(keyword));
        });
    }


    //INVENTORY TAB (ITEMS)


    private void setupInventoryTab() {
        if (tblItems == null) return;

        // Map thuộc tính từ Model Item vào các TableColumn
        colItemId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colItemName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colItemCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colItemPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colItemStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colItemStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format hiển thị giá tiền VNĐ cho cột Price
        colItemPrice.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        // Cấu hình các ComboBox bộ lọc
        if (cmbProductCategory != null) {
            cmbProductCategory.setItems(FXCollections.observableArrayList("Tất cả", "Điện tử", "Thời trang", "Gia dụng", "Khác"));
            cmbProductCategory.setValue("Tất cả");
        }
        if (cmbProductStatus != null) {
            cmbProductStatus.setItems(FXCollections.observableArrayList("Tất cả", "ĐANG BÁN", "CHỜ DUYỆT", "HẾT HÀNG"));
            cmbProductStatus.setValue("Tất cả");
        }


        tblItems.setItems(filteredItems);
        loadAllItems();

        if (txtProductSearch != null) txtProductSearch.textProperty().addListener((o, old, val) -> applyItemFilter());
        if (cmbProductCategory != null) cmbProductCategory.valueProperty().addListener((o, old, val) -> applyItemFilter());
        if (cmbProductStatus != null) cmbProductStatus.valueProperty().addListener((o, old, val) -> applyItemFilter());
    }

    private void loadAllItems() {
        new Thread(() -> {
            ApiResponse<List<Item>> resp = itemApi.getAllItems();
            Platform.runLater(() -> {
                if (resp.getStatus() == 200 && resp.getData() != null) {
                    allItems.setAll(resp.getData());
                }
            });
        }).start();
    }

    private void applyItemFilter() {
        String keyword = txtProductSearch != null ? txtProductSearch.getText().toLowerCase().trim() : "";
        String categoryFilter = cmbProductCategory != null ? cmbProductCategory.getValue() : "Tất cả";
        String statusFilter = cmbProductStatus != null ? cmbProductStatus.getValue() : "Tất cả";

        filteredItems.setPredicate(item -> {
            boolean matchSearch = keyword.isEmpty()
                    || (item.getName() != null && item.getName().toLowerCase().contains(keyword))
                    || String.valueOf(item.getId()).contains(keyword);
            boolean matchCategory = categoryFilter.equals("Tất cả")
                    || (item.getCategory() != null && item.getCategory().equalsIgnoreCase(categoryFilter));
            boolean matchStatus = statusFilter.equals("Tất cả")
                    || (item.getStatus() != null && item.getStatus().equalsIgnoreCase(statusFilter));

            return matchSearch && matchCategory && matchStatus;
        });
    }

    @FXML
    private void handleCancelProduct() {
        Item selected = tblItems.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        new Thread(() -> {
            ApiResponse<Void> resp = itemApi.deleteItem(selected.getId());
            Platform.runLater(() -> {
                if (resp.getStatus() == 200) {
                    allItems.remove(selected);
                }
            });
        }).start();
    }

    //  ORDERS TAB

    private void setupOrdersTab() {
        if (tblOrders == null) return;

        colOrderId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colOrderName.setCellValueFactory(new PropertyValueFactory<>("productName"));
        colOrderTotal.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        colOrderStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colOrderTotal.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCurrency(item));
            }
        });

        if (cmbOrderStatus != null) {
            cmbOrderStatus.setItems(FXCollections.observableArrayList("Tất cả", "SUCCESS", "PENDING", "CANCELED"));
            cmbOrderStatus.setValue("Tất cả");
        }

        tblOrders.setItems(filteredOrders);
        loadAllOrders();

        if (txtOrderSearch != null) txtOrderSearch.textProperty().addListener((o, old, val) -> applyOrderFilter());
        if (cmbOrderStatus != null) cmbOrderStatus.valueProperty().addListener((o, old, val) -> applyOrderFilter());
    }

    private void loadAllOrders() {
        new Thread(() -> {
            ApiResponse<List<Order>> resp = orderApi.getAllOrders();
            Platform.runLater(() -> {
                if (resp.getStatus() == 200 && resp.getData() != null) {
                    allOrders.setAll(resp.getData());
                }
            });
        }).start();
    }

    private void applyOrderFilter() {
        String keyword = txtOrderSearch != null ? txtOrderSearch.getText().toLowerCase().trim() : "";
        String statusFilter = cmbOrderStatus != null ? cmbOrderStatus.getValue() : "Tất cả";

        filteredOrders.setPredicate(order -> {
            boolean matchSearch = keyword.isEmpty()
                    || (order.getProductName() != null && order.getProductName().toLowerCase().contains(keyword))
                    || String.valueOf(order.getId()).contains(keyword);
            String mappedStatus = mapOrderStatus(order.getStatus());
            boolean matchStatus = statusFilter.equals("Tất cả") || statusFilter.equalsIgnoreCase(mappedStatus);

            return matchSearch && matchStatus;
        });
    }

    //AUCTIONS TAB

    private void setupAuctionsTab() {
        if (auctionsAdminFlowPane == null) return;

        if (cmbAuctionStatus != null) {
            cmbAuctionStatus.setItems(FXCollections.observableArrayList("Tất cả", "ACTIVE", "PENDING", "ENDED", "CANCELED"));
            cmbAuctionStatus.setValue("Tất cả");
        }

        if (txtAuctionSearch != null) txtAuctionSearch.textProperty().addListener((o, old, val) -> applyAuctionFilter());
        if (cmbAuctionStatus != null) cmbAuctionStatus.valueProperty().addListener((o, old, val) -> applyAuctionFilter());

        filteredAuctions.addListener((javafx.collections.ListChangeListener.Change<? extends Auction> c) -> renderAuctionsGrid());
        loadAuctionsTabFields();
    }

    private void loadAuctionsTabFields() {
        new Thread(() -> {
            ApiResponse<List<Auction>> resp = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (resp.getStatus() == 200 && resp.getData() != null) {
                    allAuctions.setAll(resp.getData());
                }
            });
        }).start();
    }

    private void applyAuctionFilter() {
        String keyword = txtAuctionSearch != null ? txtAuctionSearch.getText().toLowerCase().trim() : "";
        String statusFilter = cmbAuctionStatus != null ? cmbAuctionStatus.getValue() : "Tất cả";

        filteredAuctions.setPredicate(auction -> {
            boolean matchSearch = keyword.isEmpty() || String.valueOf(auction.getId()).contains(keyword);
            boolean matchStatus = statusFilter.equals("Tất cả")
                    || (auction.getStatus() != null && auction.getStatus().equalsIgnoreCase(statusFilter));
            return matchSearch && matchStatus;
        });
    }

    private void renderAuctionsGrid() {
        if (auctionsAdminFlowPane == null) return;
        auctionsAdminFlowPane.getChildren().clear();

        if (filteredAuctions.isEmpty()) {
            auctionsAdminFlowPane.getChildren().add(new Label("Không tìm thấy phiên đấu giá nào phù hợp."));
            return;
        }
        for (Auction auction : filteredAuctions) {
            VBox card = new VBox(8);
            card.setPrefWidth(220);
            card.setPadding(new Insets(14));
            card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2); " +
                    "-fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");

            Label lblHeader = new Label("Phiên #" + auction.getId() + " • Chủ: " + auction.getOwnerId());
            lblHeader.setStyle("-fx-font-size: 11; -fx-text-fill: #9CA3AF;");

            Label lblPriceTitle = new Label("Giá hiện tại:");
            lblPriceTitle.setStyle("-fx-font-size: 12; -fx-text-fill: #6B7280;");

            Label lblPrice = new Label(formatCurrency(auction.getCurrentPrice()));
            lblPrice.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #E74C3C;");

            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            String status = auction.getStatus() != null ? auction.getStatus().toUpperCase() : "UNKNOWN";
            String sColor = switch (status) {
                case "ACTIVE" -> "#16A34A";
                case "PENDING" -> "#F97316";
                case "ENDED" -> "#0066CC";
                default -> "#6B7280";
            };

            Label lblStatus = new Label(status);
            lblStatus.setStyle("-fx-background-color: " + sColor + "15; -fx-text-fill: " + sColor + "; -fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 11; -fx-font-weight: bold;");

            card.getChildren().addAll(lblHeader, lblPriceTitle, lblPrice, spacer, lblStatus);
            auctionsAdminFlowPane.getChildren().add(card);
        }
    }

    //ANALYTICS TAB

    private void setupAnalyticsTab() {
        if (cmbAnalyticsPeriod != null) {
            cmbAnalyticsPeriod.setItems(FXCollections.observableArrayList("7 ngày qua", "Tháng này", "Năm nay"));
            cmbAnalyticsPeriod.setValue("Tháng này");
        }
        loadRevenueChartData();
    }


    private void loadRevenueChartData() {
        if (chartMonthlyRevenue == null) return;

        new Thread(() -> {
            ApiResponse<List<Order>> resp = orderApi.getAllOrders();
            Platform.runLater(() -> {
                if (resp.getStatus() != 200 || resp.getData() == null) return;

                // Gom doanh thu theo tháng từ createdAt (format: "yyyy-MM-dd..." hoặc "yyyy-MM-dd HH:mm:ss")
                java.util.Map<String, Double> revenueByMonth = new java.util.TreeMap<>();
                for (Order o : resp.getData()) {
                    if (!"SUCCESS".equalsIgnoreCase(mapOrderStatus(o.getStatus()))) continue;
                    String month = parseMonth(o.getDate());
                    if (month == null) continue;
                    revenueByMonth.merge(month, o.getTotalAmount(), Double::sum);
                }

                javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                series.setName("Doanh thu (₫)");
                revenueByMonth.forEach((month, total) ->
                        series.getData().add(new javafx.scene.chart.XYChart.Data<>(month, total))
                );

                chartMonthlyRevenue.getData().clear();
                chartMonthlyRevenue.getData().add(series);
            });
        }).start();
    }

    // Trích tháng từ createdAt — hỗ trợ "yyyy-MM-dd" và "yyyy-MM-dd HH:mm:ss"
    private String parseMonth(String createdAt) {
        if (createdAt == null || createdAt.length() < 7) return null;
        try {
            // Lấy "yyyy-MM" → hiển thị "Tháng M/yyyy"
            String[] parts = createdAt.substring(0, 7).split("-");
            return "T" + Integer.parseInt(parts[1]) + "/" + parts[0];
        } catch (Exception e) {
            return null;
        }
    }
    // ══════════════════════════════════════════
    //  SETTINGS TAB (SECURITY ONLY)
    // ══════════════════════════════════════════

    @FXML
    private void handleChangeAdminPw() {
        if (txtAdminOldPw == null || txtAdminNewPw == null || txtAdminConfirmPw == null) return;

        String oldPw    = txtAdminOldPw.getText();
        String newPw    = txtAdminNewPw.getText();
        String confirmPw = txtAdminConfirmPw.getText();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) return;
        if (!newPw.equals(confirmPw)) return;

        new Thread(() -> {
            ApiResponse<Void> resp = userApi.changePassword(oldPw, newPw);
            Platform.runLater(() -> {
                if (resp.getStatus() == 200) {
                    txtAdminOldPw.clear();
                    txtAdminNewPw.clear();
                    txtAdminConfirmPw.clear();
                    SceneUtil.showAlert("Thành công", "Đổi mật khẩu thành công!");
                } else {
                    SceneUtil.showAlert("Thất bại", resp.getMessage());
                }
            });
        }).start();
    }

    // ══════════════════════════════════════════
    //  DASHBOARD & HELPERS
    // ══════════════════════════════════════════

    private void loadDashboardData() {
        new Thread(() -> {
            ApiResponse<List<Auction>> auctionsResp = auctionApi.getAllAuctions();
            ApiResponse<List<Order>>   ordersResp   = orderApi.getAllOrders();
            ApiResponse<List<User>>    usersResp    = userApi.getAllUsers();

            Platform.runLater(() -> {
                updateKpiCards(auctionsResp, ordersResp, usersResp);
                populateActivityFeed(ordersResp, auctionsResp);
                setupSellersPieChart(auctionsResp);
            });
        }).start();
    }

    private void updateKpiCards(ApiResponse<List<Auction>> auctionsResp,
                                ApiResponse<List<Order>>   ordersResp,
                                ApiResponse<List<User>>    usersResp) {
        // Tổng doanh thu
        if (ordersResp.getStatus() == 200 && ordersResp.getData() != null) {
            double totalRevenue = ordersResp.getData().stream().mapToDouble(Order::getTotalAmount).sum();
            if (lblTotalRevenue != null) lblTotalRevenue.setText(formatCurrency(totalRevenue));
        }
        // Tổng người dùng — đúng từ getAllUsers()
        if (usersResp.getStatus() == 200 && usersResp.getData() != null) {
            long userCount = usersResp.getData().stream()
                    .filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole()))
                    .count();
            if (lblTotalUsers != null) lblTotalUsers.setText(userCount + " người dùng");
        } else {
            if (lblTotalUsers != null) lblTotalUsers.setText("--");
        }
        // Phiên đấu giá + số người bán
        if (auctionsResp.getStatus() == 200 && auctionsResp.getData() != null) {
            long activeCount = auctionsResp.getData().stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
            long sellerCount = auctionsResp.getData().stream().map(Auction::getOwnerId).distinct().count();
            if (lblTotalAuctions != null) lblTotalAuctions.setText(activeCount + " đang chạy");
            if (lblTotalSellers  != null) lblTotalSellers.setText(String.valueOf(sellerCount));
        }
    }

    private void populateActivityFeed(ApiResponse<List<Order>> ordersResp, ApiResponse<List<Auction>> auctionsResp) {
        if (activityVBox == null) return;
        activityVBox.getChildren().clear();

        if (ordersResp.getStatus() == 200 && ordersResp.getData() != null) {
            ordersResp.getData().stream().limit(4).forEach(order -> {
                Label lbl = new Label("Đơn #" + order.getId() + " - " + order.getProductName() + " - " + formatCurrency(order.getTotalAmount()));
                activityVBox.getChildren().add(lbl);
            });
        }
    }

    private void setupSellersPieChart(ApiResponse<List<Auction>> auctionsResp) {
        if (chartSellers == null) return;
        chartSellers.getData().clear();
        if (auctionsResp.getStatus() == 200 && auctionsResp.getData() != null) {
            long active = auctionsResp.getData().stream().filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus())).count();
            if (active > 0) chartSellers.getData().add(new PieChart.Data("Đang chạy", active));
        }
    }

    private void populateAdminInfo() {
        User user = SessionManager.getCurrentUser();
        if (user != null && welcomeLabel != null) {
            welcomeLabel.setText(user.getUsername() != null ? user.getUsername() : "Admin");
        }
    }

    @FXML private void handleHome()      { switchTab(tabDashboard,  "Dashboard",          btnHome);      }
    @FXML private void handleUsers()     { switchTab(tabUsers,      "Quản Lý Người Dùng", btnUsers);     loadAllUsers(); }
    @FXML private void handleSellers()   { switchTab(tabSellers,    "Quản Lý Người Bán",  btnSellers);   loadAllSellers(); }
    @FXML private void handleInventory() { switchTab(tabInventory,  "Quản Lý Sản Phẩm",   btnInventory); loadAllItems(); }
    @FXML private void handleOrders()    { switchTab(tabOrders,     "Đơn Hàng",           btnOrders);    loadAllOrders(); }
    @FXML private void handleAuctions()  { switchTab(tabAuctions,   "Đấu Giá",            btnAuctions);  loadAuctionsTabFields(); }
    @FXML private void handleAnalytics() { switchTab(tabAnalytics,  "Phân Tích",          btnAnalytics); }
    @FXML private void handleSettings()  { switchTab(tabSettings,   "Cài Đặt",            btnSettings);  }

    @FXML
    private void handleSignOut() {
        SessionManager.clear();
        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
    }

    @FXML private void handleRefreshAnalytics() {loadRevenueChartData();}

    private String formatCurrency(double amount) { return String.format("%,.0f ₫", amount); }

    private String mapOrderStatus(String raw) {
        if (raw == null) return "UNKNOWN";
        return switch (raw.toUpperCase()) {
            case "COMPLETED", "PAID" -> "SUCCESS";
            case "PENDING"           -> "PENDING";
            default                  -> "CANCELED";
        };
    }

    private final String NORMAL_STYLE = "-fx-background-color: transparent; -fx-text-fill: #CBD5E1; -fx-background-radius: 8; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14;";
    private final String ACTIVE_STYLE = "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 8; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 14;";

    private void switchTab(Tab tab, String title, Button activeBtn) {
        if (mainTabPane != null && tab != null) mainTabPane.getSelectionModel().select(tab);
        if (lblPageTitle != null && title != null) lblPageTitle.setText(title);
        highlightButton(activeBtn);
    }

    private void highlightButton(Button active) {
        if (sidebarButtons == null) return;
        for (Button b : sidebarButtons) if (b != null) b.setStyle(NORMAL_STYLE);
        if (active != null) active.setStyle(ACTIVE_STYLE);
    }
}
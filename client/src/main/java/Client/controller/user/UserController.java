package Client.controller.user;

import Client.model.Notification;
import Client.model.auction.Auction;
import Client.model.auction.Bid;
import Client.model.auction.BidHistoryItem;
import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.websocket.AuctionWebSocketClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import Client.networking.endpoints.AuctionApi;
import Client.networking.endpoints.BidApi;
import Client.networking.endpoints.NotificationApi;
import Client.networking.endpoints.UserApi;
import Client.controller.user.dialogs.AuctionDetailDialog;
import Client.controller.user.dialogs.DepositDialog;
import Client.controller.user.helpers.AuctionCardBuilder;
import Client.controller.user.helpers.BidHistoryRenderer;
import Client.controller.user.helpers.NotificationRenderer;
import Client.util.DialogUtil;
import Client.controller.user.dialogs.AutoBidDialog;
import Client.controller.user.dialogs.BidHistoryDialog;
import Client.controller.user.dialogs.PlaceBidDialog;
import Client.util.SceneUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserController {
    //FXML — Top Navbar
    @FXML private TextField txtSearch;
    @FXML private Button    btnNotifTop;
    @FXML private Label     lblNotifCount;
    @FXML private Label     lblBalance;
    @FXML private Label     lblUsername;
    @FXML private Circle    avatarCircle;
    //FXML — Sidebar buttons
    @FXML private Button btnDashBoard;
    @FXML private Button btnAuction;
    @FXML private Button btnOrderHistory;
    @FXML private Button btnNotification;
    @FXML private Button btnProfile;
    @FXML private Button btnSettings;
    @FXML private Button btnSignOut;
    @FXML private Label  lblSidebarName;
    //FXML — TabPane
    @FXML private TabPane mainTabPane;
    @FXML private Tab tabDashboard;
    @FXML private Tab tabBidHistory;
    @FXML private Tab tabNotification;
    @FXML private Tab tabProfile;
    @FXML private Tab tabSettings;
    //FXML — Tab Dashboard
    @FXML private Label              lblActiveBids;
    @FXML private Label              lblWonAuctions;
    @FXML private ComboBox<String>   cmbFilter;
    @FXML private FlowPane           auctionFlowPane;
    @FXML private Button             btnRefreshAuctions;

    //  Pagination controls 
    @FXML private Button btnPrevPage;
    @FXML private Button btnNextPage;
    @FXML private Label  lblPageInfo;
    //FXML — Tab Notification
    @FXML private Button btnMarkAllRead;
    @FXML private Button btnNotifAll;
    @FXML private Button btnNotifUnread;
    @FXML private Button btnNotifAuction;
    @FXML private Button btnNotifOrder;
    @FXML private VBox   notificationList;
    //FXML — Tab Profile
    @FXML private Label         lblProfileName;
    @FXML private Label         lblProfileEmail;
    @FXML private Button        btnChangeAvatar;
    @FXML private TextField     txtFullName;
    @FXML private TextField     txtPhone;
    @FXML private TextField     txtEmail;
    @FXML private TextField     txtAddress;
    @FXML private Button        btnSaveProfile;
    @FXML private PasswordField txtOldPassword;
    @FXML private PasswordField txtNewPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private Button        btnChangePassword;
    //FXML — Tab Settings
    @FXML private Button toggleAuctionNotif;
    @FXML private Button toggleEmailNotif;
    @FXML private Button btnDeleteAccount;
    //  Tab Bid History 
    @FXML private ComboBox<String> cmbBidHistoryFilter;
    @FXML private VBox             bidHistoryList;
    //APIs & Data
    private final UserApi         userApi    = new UserApi();
    private final AuctionApi      auctionApi = new AuctionApi();
    private final BidApi          bidApi     = new BidApi();
    private final NotificationApi notifApi   = new NotificationApi();

    private final ObservableList<Auction> liveAuctions        = FXCollections.observableArrayList();
    private final ObservableList<Auction> myBiddingAuctions   = FXCollections.observableArrayList();
    /** Whichever list is currently rendered — used by pagination. */
    private List<Auction> currentDisplayList = liveAuctions;
    /** auctionId → the price Label inside that card, for targeted live updates */
    private final Map<Integer, Label>     livePriceLabels  = new ConcurrentHashMap<>();
    /** auctionId → the time-remaining Label inside that card, updated on anti-snipe reset */
    private final Map<Integer, Label>     liveTimeLabels   = new ConcurrentHashMap<>();
    /** auctionId → the status badge Label ("• Đang diễn ra") on the card */
    private final Map<Integer, Label>     liveStatusLabels = new ConcurrentHashMap<>();
    /** auctionId → the "Đặt giá ngay" Button on the card */
    private final Map<Integer, Button>    liveBidButtons   = new ConcurrentHashMap<>();
    /** auctionId → the "Đặt giá tự động" Button on the card */
    private final Map<Integer, Button>    liveAutoBidBtns  = new ConcurrentHashMap<>();
    /** auctionId → price updater for the currently open AuctionDetailDialog (if any) */
    private final Map<Integer, Consumer<Double>> liveDialogPriceUpdaters = new ConcurrentHashMap<>();
    /** auctionId → the highest-bidder Label on the card */
    private final Map<Integer, Label> liveHighestBidderLabels = new ConcurrentHashMap<>();
    /** Ticks every second to keep countdown labels fresh and flip expired cards to FINISHED state. */
    private Timeline countdownTicker;
    /** Single persistent WebSocket connection for the auction floor */
    private AuctionWebSocketClient wsClient;

    /** Cached bid history items so the filter ComboBox can re-filter without a network call. */
    private List<BidHistoryItem> cachedBidHistory = List.of();

    /** Cached notifications so filter buttons can re-render without a network call. */
    private List<Notification> cachedNotifications = List.of();


    //  Pagination 
    private static final int PAGE_SIZE = 21;
    private int currentPage = 0;
    /** The filtered (but not yet paged) list — kept so next/prev can slice it. */
    private List<Auction> filteredAuctions = List.of();

    /** Lazy-initialized after FXML injection. Builds one card VBox per auction. */
    private AuctionCardBuilder cardBuilder;
    //Initialize
    @FXML
    public void initialize() {
        // Card builder — created after FXML injection so the scene/window is reachable lazily
        cardBuilder = new AuctionCardBuilder(new AuctionCardBuilder.Config(
                () -> mainTabPane.getScene() != null ? mainTabPane.getScene().getWindow() : null,
                bidApi,
                livePriceLabels, liveStatusLabels, liveTimeLabels,
                liveBidButtons, liveAutoBidBtns,
                liveDialogPriceUpdaters, liveHighestBidderLabels,
                this::onBidSuccess,
                this::formatTimeRemaining));

        setupComboBoxes();
        populateUserInfo();
        loadMyBiddingAuctions();   // Dashboard is the landing tab — show user's own bids
        loadNotifications();

        //Highlight Dashboard as the default active sidebar button
        setActiveNavButton(btnDashBoard);

        // Re-filter auction cards whenever the ComboBox value changes; reset to page 0
        cmbFilter.valueProperty().addListener((obs, old, val) -> {
            currentPage = 0;
            renderAuctionCards(currentDisplayList);
        });

        // Re-filter bid history when outcome filter changes
        if (cmbBidHistoryFilter != null) {
            cmbBidHistoryFilter.valueProperty().addListener((obs, old, val) ->
                    renderBidHistoryCards(cachedBidHistory));
        }
    }

    /** Reads the User saved by LoginController and fills every label/field that shows user data. */
    private void populateUserInfo() {
        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        String displayName = user.getUsername() != null ? user.getUsername() : "—";
        String email       = user.getEmail()    != null ? user.getEmail()    : "—";

        if (lblUsername    != null) lblUsername.setText(displayName);
        if (lblSidebarName != null) lblSidebarName.setText(displayName);
        if (lblProfileName != null) lblProfileName.setText(displayName);
        if (lblProfileEmail != null) lblProfileEmail.setText(email);
        if (txtFullName    != null) txtFullName.setText(displayName);
        if (txtEmail       != null) txtEmail.setText(email);
        updateBalanceLabel(user.getBalance());

        // Apply saved notification preferences to the toggle buttons
        applyToggleState(toggleAuctionNotif, user.isNotifAuction());
        applyToggleState(toggleEmailNotif,   user.isNotifEmail());
        // toggleOrderNotif and toggle2FA are UI-only (no backend), keep their initial FXML state
    }

    /** Sets a toggle button's text and colour to match the given on/off state. */
    private void applyToggleState(Button btn, boolean on) {
        if (btn == null) return;
        if (on) {
            btn.setText("Bật");
            btn.setStyle("-fx-background-color: #16A34A; -fx-text-fill: white; "
                    + "-fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;");
        } else {
            btn.setText("Tắt");
            btn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; "
                    + "-fx-background-radius: 12; -fx-padding: 4 12; -fx-cursor: hand;");
        }
    }

    //Setup helpers
    private void setupComboBoxes() {
        cmbFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", "Đang chạy", "Sắp diễn ra", "Đã kết thúc"));
        if (cmbBidHistoryFilter != null) {
            cmbBidHistoryFilter.setItems(FXCollections.observableArrayList(
                    "Tất cả", "Đã thắng", "Đã thua"));
            cmbBidHistoryFilter.getSelectionModel().selectFirst();
        }
    }
    //Sidebar Navigation
    @FXML private void handleDashBoard()    { switchTab(tabDashboard,   btnDashBoard);   loadMyBiddingAuctions(); }
    @FXML private void handleAuction()      { switchTab(tabDashboard,   btnAuction);     loadAuctions(); }
    @FXML private void handleNotification() { switchTab(tabNotification, btnNotification); loadNotifications(); }
    @FXML private void handleProfile()      { switchTab(tabProfile,      btnProfile); }
    @FXML private void handleSettings()     { switchTab(tabSettings,     btnSettings); }
    @FXML private void handleHistory()      { switchTab(tabBidHistory,   btnOrderHistory); loadBidHistory(); }
    @FXML private void handleRefreshBidHistory() { loadBidHistory(); }


    @FXML
    private void handleSignOut() {
        if (wsClient != null) wsClient.closeConnection();
        SessionManager.clear();
        SceneUtil.switchToScene(btnSignOut, "/Client/views/LoginView.fxml", "Login");
    }
    //Auction Floor — Load & Render

    /** Fetches all auctions on a background thread, then rebuilds the card grid. */
    @FXML
    private void handleRefreshAuctions() { loadAuctions(); }

    private void loadAuctions() {
        if (auctionFlowPane == null) return;

        auctionFlowPane.getChildren().clear();
        Label loading = new Label("⏳ Đang tải danh sách phiên đấu giá...");
        loading.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
        auctionFlowPane.getChildren().add(loading);

        if (btnRefreshAuctions != null) btnRefreshAuctions.setDisable(true);

        new Thread(() -> {
            ApiResponse<List<Auction>> response = auctionApi.getAllAuctions();
            Platform.runLater(() -> {
                if (btnRefreshAuctions != null) btnRefreshAuctions.setDisable(false);

                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    liveAuctions.setAll(response.getData());
                    // Stop any running ticker before clearing maps
                    if (countdownTicker != null) { countdownTicker.stop(); countdownTicker = null; }
                    livePriceLabels.clear();
                    liveTimeLabels.clear();
                    liveStatusLabels.clear();
                    liveBidButtons.clear();
                    liveAutoBidBtns.clear();
                    liveHighestBidderLabels.clear();
                    currentDisplayList = liveAuctions;
                    currentPage = 0;
                    renderAuctionCards(currentDisplayList);
                    updateAuctionStats();
                    connectWebSocket();   // subscribe to all active auctions
                    startCountdownTicker(); // tick every second to keep timers fresh
                } else {
                    auctionFlowPane.getChildren().clear();
                    String msg = response != null ? response.getMessage() : "Mất kết nối tới Server";
                    Label err = new Label("❌ Không thể tải phiên đấu giá: " + msg);
                    err.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13;");
                    auctionFlowPane.getChildren().add(err);
                }
            });
        }).start();
    }

    /** Fetches only auctions the current user has placed bids on. */
    private void loadMyBiddingAuctions() {
        if (auctionFlowPane == null) return;

        auctionFlowPane.getChildren().clear();
        Label loading = new Label("⏳ Đang tải phiên đấu giá của bạn...");
        loading.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
        auctionFlowPane.getChildren().add(loading);

        new Thread(() -> {
            ApiResponse<List<Auction>> response = bidApi.getMyBiddingAuctions();
            Platform.runLater(() -> {
                auctionFlowPane.getChildren().clear();
                if (response != null && response.getStatus() == 200 && response.getData() != null) {
                    myBiddingAuctions.setAll(response.getData());
                    currentDisplayList = myBiddingAuctions;
                    currentPage = 0;
                    if (myBiddingAuctions.isEmpty()) {
                        Label empty = new Label("Bạn chưa tham gia phiên đấu giá nào.");
                        empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
                        auctionFlowPane.getChildren().add(empty);
                    } else {
                        renderAuctionCards(currentDisplayList);
                    }
                    updateAuctionStats();
                    connectWebSocket();
                    if (countdownTicker == null || countdownTicker.getStatus() != javafx.animation.Animation.Status.RUNNING) {
                        startCountdownTicker();
                    }
                } else {
                    String msg = response != null ? response.getMessage() : "Mất kết nối tới Server";
                    Label err = new Label("❌ Không thể tải dữ liệu: " + msg);
                    err.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13;");
                    auctionFlowPane.getChildren().add(err);
                }
            });
        }).start();
    }

    /** Opens (or reuses) the WebSocket connection and subscribes to every ACTIVE auction. */
    private void connectWebSocket() {
        // Subscribe to active auctions from both lists — liveAuctions (Auction tab)
        // and myBiddingAuctions (Dashboard tab) — so updates work regardless of which
        // tab triggered the load.
        List<Integer> activeIds = Stream.concat(
                        liveAuctions.stream(), myBiddingAuctions.stream())
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .map(Auction::getId)
                .distinct()
                .toList();

        // If the connection is already alive just re-subscribe — don't tear it down
        if (wsClient != null && wsClient.isOpen()) {
            activeIds.forEach(wsClient::subscribe);
            System.out.println("[WS Client] Re-subscribed to " + activeIds.size() + " auction(s): " + activeIds);
            return;
        }

        // Close a stale (not-yet-open or errored) connection before creating a new one
        if (wsClient != null && !wsClient.isClosed()) wsClient.closeConnection();

        wsClient = new AuctionWebSocketClient(update -> {
            int    auctionId          = update.auctionId();
            double newPrice           = update.newPrice();
            String newEndTime         = update.newEndTime();
            String highestBidderName  = update.highestBidderName();

            // Update the in-memory auction object in both lists
            Stream.concat(liveAuctions.stream(), myBiddingAuctions.stream())
                    .filter(a -> a.getId() == auctionId)
                    .forEach(a -> {
                        a.setCurrentPrice(newPrice);
                        if (newEndTime != null) a.setEndTime(newEndTime);
                    });

            // Update price label on the card
            Label priceLabel = livePriceLabels.get(auctionId);
            if (priceLabel != null) {
                priceLabel.setText("Giá hiện tại: " + String.format("%,.0f ₫", newPrice));
                priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #16A34A;");
            }

            // Update highest bidder label on the card
            if (highestBidderName != null) {
                Label bidderLabel = liveHighestBidderLabels.get(auctionId);
                if (bidderLabel != null) bidderLabel.setText("🏆 " + highestBidderName);
                Stream.concat(liveAuctions.stream(), myBiddingAuctions.stream())
                        .filter(a -> a.getId() == auctionId)
                        .forEach(a -> a.setHighestBidderName(highestBidderName));
            }

            // Update the open detail dialog for this auction (if any)
            Consumer<Double> dialogUpdater = liveDialogPriceUpdaters.get(auctionId);
            if (dialogUpdater != null) dialogUpdater.accept(newPrice);

            // Anti-snipe: if the server reset the timer, update the time label live
            if (newEndTime != null) {
                Label timeLabel = liveTimeLabels.get(auctionId);
                if (timeLabel != null) {
                    timeLabel.setUserData(newEndTime);   // keep ticker in sync
                    timeLabel.setText("🕒 " + formatTimeRemaining(newEndTime));
                    // Flash orange briefly to draw attention to the reset
                    timeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #D97706; -fx-font-weight: bold;");
                    javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                            javafx.util.Duration.seconds(3));
                    pause.setOnFinished(e -> timeLabel.setStyle(
                            "-fx-font-size: 11; -fx-text-fill: #0066CC;"));
                    pause.play();
                }
            }
        });

        // subscribe() now tracks rooms internally and re-subscribes automatically on reconnect.
        activeIds.forEach(wsClient::subscribe);
        wsClient.connect();
    }

    // ticks every second — updates the countdown labels and flips cards to "ended"
    // when the end time is reached (disables bid buttons, updates status badge)
    private void startCountdownTicker() {
        countdownTicker = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            java.time.LocalDateTime now = java.time.LocalDateTime.now(java.time.ZoneOffset.UTC);
            // Iterate over a snapshot to avoid ConcurrentModificationException
            new java.util.HashMap<>(liveTimeLabels).forEach((auctionId, timeLabel) -> {
                String endTimeStr = (String) timeLabel.getUserData();
                if (endTimeStr == null) return;

                try {
                    java.time.LocalDateTime endTime = java.time.LocalDateTime.parse(endTimeStr);
                    if (!now.isBefore(endTime)) {
                        // Auction just expired — flip the card UI
                        timeLabel.setText("🕒 Đã kết thúc");
                        timeLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

                        Label statusLabel = liveStatusLabels.get(auctionId);
                        if (statusLabel != null) {
                            statusLabel.setText("✓ Đã kết thúc");
                            statusLabel.setStyle("-fx-text-fill: #6B7280; -fx-font-size: 11; -fx-font-weight: bold;");
                        }

                        Button bidBtn = liveBidButtons.get(auctionId);
                        if (bidBtn != null) {
                            bidBtn.setDisable(true);
                            bidBtn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 8;");
                        }

                        Button autoBidBtn = liveAutoBidBtns.get(auctionId);
                        if (autoBidBtn != null) {
                            autoBidBtn.setDisable(true);
                            autoBidBtn.setStyle("-fx-background-color: #9CA3AF; -fx-text-fill: white; " +
                                    "-fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 7;");
                        }

                        // Remove from all live maps — no more updates needed for this auction
                        liveTimeLabels.remove(auctionId);
                        liveStatusLabels.remove(auctionId);
                        livePriceLabels.remove(auctionId);
                        liveBidButtons.remove(auctionId);
                        liveAutoBidBtns.remove(auctionId);
                    } else {
                        // Still running — refresh the label text
                        timeLabel.setText("🕒 " + formatTimeRemaining(endTimeStr));
                    }
                } catch (Exception ex) {
                    // Unparseable endTime — leave label as-is
                }
            });
        }));
        countdownTicker.setCycleCount(Timeline.INDEFINITE);
        countdownTicker.play();
    }

    /** Applies the current filter + current page and rebuilds the FlowPane cards. Must run on FX thread. */
    private void renderAuctionCards(List<Auction> auctions) {
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();

        String filter = cmbFilter.getValue();
        filteredAuctions = auctions.stream().filter(a -> {
            String s = a.getStatus() != null ? a.getStatus().toUpperCase() : "";
            if (filter == null || filter.equals("Tất cả")) return true;
            return switch (filter) {
                case "Đang chạy"   -> s.equals("ACTIVE");
                case "Sắp diễn ra" -> s.equals("UPCOMING");
                case "Đã kết thúc" -> s.equals("FINISHED");
                default -> true;
            };
        }).sorted(java.util.Comparator.comparingInt(a -> statusPriority(a.getStatus())))
          .collect(Collectors.toList());

        if (filteredAuctions.isEmpty()) {
            Label empty = new Label("Không có phiên đấu giá nào phù hợp.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            auctionFlowPane.getChildren().add(empty);
            updatePaginationControls(0, 0);
            return;
        }

        //  Page slice 
        int totalPages = (int) Math.ceil((double) filteredAuctions.size() / PAGE_SIZE);
        if (currentPage >= totalPages) currentPage = totalPages - 1;
        if (currentPage < 0)          currentPage = 0;

        int from = currentPage * PAGE_SIZE;
        int to   = Math.min(from + PAGE_SIZE, filteredAuctions.size());

        for (Auction auction : filteredAuctions.subList(from, to)) {
            auctionFlowPane.getChildren().add(buildAuctionCard(auction));
        }
        updatePaginationControls(currentPage, totalPages);
    }

    private static int statusPriority(String s) {
        if (s == null) return 2;
        return switch (s.toUpperCase()) {
            case "ACTIVE"   -> 0;
            case "UPCOMING" -> 1;
            default         -> 2; // FINISHED and anything else sink to the bottom
        };
    }

    private void updatePaginationControls(int page, int total) {
        if (lblPageInfo  != null) lblPageInfo.setText(
                total == 0 ? "—" : "Trang " + (page + 1) + " / " + total);
        if (btnPrevPage  != null) btnPrevPage.setDisable(page <= 0);
        if (btnNextPage  != null) btnNextPage.setDisable(total == 0 || page >= total - 1);
    }

    @FXML private void handlePrevPage() {
        if (currentPage > 0) { currentPage--; renderAuctionCards(currentDisplayList); }
    }

    @FXML private void handleNextPage() {
        int total = (int) Math.ceil((double) filteredAuctions.size() / PAGE_SIZE);
        if (currentPage < total - 1) { currentPage++; renderAuctionCards(currentDisplayList); }
    }

    /** Builds one auction card VBox — delegates to AuctionCardBuilder. */
    private VBox buildAuctionCard(Auction auction) { return cardBuilder.build(auction); }

    @SuppressWarnings("unused") // full impl moved to AuctionCardBuilder — kept as reference
    private String formatTimeRemaining(String endTimeStr) {
        if (endTimeStr == null) return "Không xác định";
        try {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
            if (!now.isBefore(endTime)) return "Đã kết thúc";
            Duration diff = Duration.between(now, endTime);
            long days    = diff.toDays();
            long hours   = diff.toHoursPart();
            long minutes = diff.toMinutesPart();
            if (days > 0) return String.format("Còn %d ngày %02d:%02d", days, hours, minutes);
            return String.format("Còn %02d giờ %02d phút", hours, minutes);
        } catch (Exception e) {
            return endTimeStr;  // fallback: show raw string
        }
    }

    /** Updates the dashboard stat cards with live counts. */
    private void updateAuctionStats() {
        String myName = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getUsername() : null;

        long active = myBiddingAuctions.stream()
                .filter(a -> "ACTIVE".equalsIgnoreCase(a.getStatus()))
                .count();
        long won = myName == null ? 0 : myBiddingAuctions.stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus())
                        && myName.equals(a.getHighestBidderName()))
                .count();

        if (lblActiveBids  != null) lblActiveBids.setText(active + " phiên");
        if (lblWonAuctions != null) lblWonAuctions.setText(won + " phiên");
    }

    //Bidding callbacks

    /** Called by PlaceBidDialog after a successful bid — updates balance; WebSocket handles price/bidder updates. */
    private void onBidSuccess(double newBalance) {
        User u = SessionManager.getCurrentUser();
        if (u != null) u.setBalance(newBalance);
        updateBalanceLabel(newBalance);
        updateAuctionStats();
    }

    // (handlePlaceBid, handleAutoBid, showBidHistory, showAuctionDetail
    //  and their helpers have been moved to Client.controller.user.dialogs)

    // Search
    @FXML
    private void handleSearch() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        if (keyword.isEmpty()) {
            renderAuctionCards(liveAuctions);
            return;
        }
        List<Auction> filtered = currentDisplayList.stream()
                .filter(a -> String.valueOf(a.getId()).contains(keyword)
                        || String.valueOf(a.getItemId()).contains(keyword))
                .collect(Collectors.toList());
        currentPage = 0;
        switchTab(tabDashboard, btnDashBoard);
        if (auctionFlowPane == null) return;
        auctionFlowPane.getChildren().clear();
        if (filtered.isEmpty()) {
            Label empty = new Label("Không tìm thấy phiên đấu giá phù hợp.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            auctionFlowPane.getChildren().add(empty);
            return;
        }
        for (Auction auction : filtered) {
            auctionFlowPane.getChildren().add(buildAuctionCard(auction));
        }
    }
    //Notification
    @FXML private void handleMarkAllRead() {
        new Thread(() -> {
            notifApi.markAllRead();
            Platform.runLater(this::loadNotifications);
        }).start();
    }
    @FXML private void filterNotifAll() {
        setActiveNotifFilter(btnNotifAll);
        renderNotifications(cachedNotifications);
    }

    @FXML private void filterNotifUnread() {
        setActiveNotifFilter(btnNotifUnread);
        List<Notification> unread = cachedNotifications.stream()
                .filter(n -> !n.isRead()).toList();
        renderNotifications(unread);
    }

    @FXML private void filterNotifAuction() {
        setActiveNotifFilter(btnNotifAuction);
        renderNotifications(cachedNotifications.stream()
                .filter(n -> NotificationRenderer.isAuctionNotif(n.getMessage())).toList());
    }

    @FXML private void filterNotifOrder() {
        setActiveNotifFilter(btnNotifOrder);
        renderNotifications(cachedNotifications.stream()
                .filter(n -> !NotificationRenderer.isAuctionNotif(n.getMessage())).toList());
    }

    /** Visually highlights the active filter button and resets all others. */
    private void setActiveNotifFilter(Button active) {
        Button[] filters = { btnNotifAll, btnNotifUnread, btnNotifAuction, btnNotifOrder };
        for (Button btn : filters) {
            if (btn == null) continue;
            if (btn == active) {
                btn.setStyle("-fx-background-color: #0066CC; -fx-text-fill: white; "
                        + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
            } else {
                btn.setStyle("-fx-background-color: #F3F4F6; -fx-text-fill: #374151; "
                        + "-fx-background-radius: 20; -fx-padding: 5 15; -fx-cursor: hand;");
            }
        }
    }

    private void loadNotifications() {
        if (notificationList == null) return;
        new Thread(() -> {
            ApiResponse<List<Notification>> resp = notifApi.getNotifications();
            Platform.runLater(() -> {
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    cachedNotifications = resp.getData();
                    long unread = cachedNotifications.stream().filter(n -> !n.isRead()).count();
                    if (lblNotifCount != null) lblNotifCount.setText(unread > 0 ? String.valueOf(unread) : "0");
                } else {
                    cachedNotifications = List.of();
                    if (lblNotifCount != null) lblNotifCount.setText("0");
                }
                // Reset to "All" filter and render
                setActiveNotifFilter(btnNotifAll);
                renderNotifications(cachedNotifications);
            });
        }).start();
    }

    /** Renders the given list into the notification VBox. */
    private void renderNotifications(List<Notification> list) {
        NotificationRenderer.render(list, notificationList);
    }

    // (buildNotifRow, formatNotifTime, isAuctionNotif moved to NotificationRenderer)
    //Profile
    @FXML
    private void handleSaveProfile() {
        String name  = txtFullName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || phone.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng điền đầy đủ họ tên, số điện thoại và email.");
            return;
        }
        if (lblUsername    != null) lblUsername.setText(name);
        if (lblSidebarName != null) lblSidebarName.setText(name);
        if (lblProfileName != null) lblProfileName.setText(name);
        if (lblProfileEmail != null) lblProfileEmail.setText(email);

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            user.setUsername(name);
            user.setEmail(email);
        }
        // TODO: send update to server
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thông tin cá nhân đã được cập nhật.");
    }

    @FXML
    private void handleChangePassword() {
        String oldPw     = txtOldPassword.getText();
        String newPw     = txtNewPassword.getText();
        String confirmPw = txtConfirmPassword.getText();

        if (oldPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin",
                    "Vui lòng điền đầy đủ các ô mật khẩu.");
            return;
        }
        if (!newPw.equals(confirmPw)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu mới và xác nhận không khớp.");
            return;
        }
        if (newPw.length() < 6) {
            showAlert(Alert.AlertType.WARNING, "Mật khẩu quá ngắn",
                    "Mật khẩu mới phải có ít nhất 6 ký tự.");
            return;
        }

        btnChangePassword.setDisable(true);
        btnChangePassword.setText("Đang xử lý...");

        new Thread(() -> {
            ApiResponse<Void> resp = userApi.changePassword(oldPw, newPw);
            Platform.runLater(() -> {
                btnChangePassword.setDisable(false);
                btnChangePassword.setText("Đổi Mật Khẩu");
                if (resp != null && resp.getStatus() == 200) {
                    txtOldPassword.clear();
                    txtNewPassword.clear();
                    txtConfirmPassword.clear();
                    showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Mật khẩu đã được thay đổi.");
                } else {
                    String msg = resp != null ? resp.getMessage() : "Mất kết nối tới Server";
                    showAlert(Alert.AlertType.ERROR, "Đổi mật khẩu thất bại", msg);
                }
            });
        }).start();
    }

    //Settings
    @FXML
    private void handleDeleteAccount() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xóa tài khoản");
        confirm.setHeaderText("Bạn có chắc muốn xóa tài khoản?");
        confirm.setContentText("Hành động này không thể hoàn tác. Toàn bộ dữ liệu của bạn sẽ bị xóa vĩnh viễn.");
        confirm.showAndWait().ifPresent(response -> {
            if (response != ButtonType.OK) return;

            btnDeleteAccount.setDisable(true);
            new Thread(() -> {
                ApiResponse<Void> resp = userApi.deleteAccount();
                Platform.runLater(() -> {
                    btnDeleteAccount.setDisable(false);
                    if (resp != null && resp.getStatus() == 200) {
                        if (wsClient != null) wsClient.closeConnection();
                        SessionManager.clear();
                        SceneUtil.switchToScene(btnDeleteAccount, "/Client/views/LoginView.fxml", "Login");
                    } else {
                        String msg = resp != null ? resp.getMessage() : "Mất kết nối tới Server";
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xóa tài khoản: " + msg);
                    }
                });
            }).start();
        });
    }

    @FXML
    private void handleToggleNotification(ActionEvent event) {
        Button btn = (Button) event.getSource();
        boolean turningOn = btn.getText().equals("Tắt"); // currently off → turning on
        applyToggleState(btn, turningOn);

        // Only persist changes for auction and email toggles (order + 2FA are UI-only)
        if (btn != toggleAuctionNotif && btn != toggleEmailNotif) return;

        User user = SessionManager.getCurrentUser();
        if (user == null) return;

        // Compute new prefs from the current button states after toggling
        boolean notifAuction = toggleAuctionNotif != null && toggleAuctionNotif.getText().equals("Bật");
        boolean notifEmail   = toggleEmailNotif   != null && toggleEmailNotif.getText().equals("Bật");

        // Update local session so the state is correct if the user re-opens settings
        user.setNotifAuction(notifAuction);
        user.setNotifEmail(notifEmail);

        new Thread(() -> {
            ApiResponse<Void> resp = userApi.updatePreferences(notifAuction, notifEmail);
            if (resp == null || resp.getStatus() != 200) {
                // Revert UI on failure
                Platform.runLater(() -> {
                    applyToggleState(btn, !turningOn);
                    if (btn == toggleAuctionNotif) user.setNotifAuction(!turningOn);
                    else                           user.setNotifEmail(!turningOn);
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lưu tùy chọn thông báo.");
                });
            }
        }).start();
    }
    //Balance & Deposit

    /** Updates the balance chip in the top bar. Must be called on the FX thread. */
    private void updateBalanceLabel(double balance) {
        if (lblBalance != null)
            lblBalance.setText(String.format("%,.0f ₫", balance));
    }

    @FXML
    private void handleDeposit() {
        User user = SessionManager.getCurrentUser();
        double bal = user != null ? user.getBalance() : 0;
        DepositDialog.show(bal, userApi, newBalance -> {
            if (user != null) user.setBalance(newBalance);
            updateBalanceLabel(newBalance);
        });
    }

    //Bid History

    private void loadBidHistory() {
        if (bidHistoryList == null) return;
        bidHistoryList.getChildren().clear();
        Label loading = new Label("⏳ Đang tải lịch sử đặt giá...");
        loading.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13;");
        bidHistoryList.getChildren().add(loading);

        new Thread(() -> {
            ApiResponse<List<BidHistoryItem>> resp = bidApi.getMyBidHistory();
            Platform.runLater(() -> {
                bidHistoryList.getChildren().clear();
                if (resp != null && resp.getStatus() == 200 && resp.getData() != null) {
                    cachedBidHistory = resp.getData();
                    renderBidHistoryCards(cachedBidHistory);
                } else {
                    String msg = resp != null ? resp.getMessage() : "Mất kết nối tới Server";
                    Label err = new Label("❌ Không thể tải lịch sử: " + msg);
                    err.setStyle("-fx-text-fill: #D32F2F; -fx-font-size: 13;");
                    bidHistoryList.getChildren().add(err);
                }
            });
        }).start();
    }

    private void renderBidHistoryCards(List<BidHistoryItem> items) {
        if (bidHistoryList == null) return;
        bidHistoryList.getChildren().clear();

        String filter = cmbBidHistoryFilter != null ? cmbBidHistoryFilter.getValue() : "Tất cả";
        List<BidHistoryItem> filtered = items.stream().filter(it -> {
            if (filter == null || "Tất cả".equals(filter)) return true;
            return "Đã thắng".equals(filter) ? it.isWon() : !it.isWon();
        }).toList();

        if (filtered.isEmpty()) {
            Label empty = new Label("Chưa có lịch sử đặt giá nào.");
            empty.setStyle("-fx-text-fill: #9CA3AF; -fx-font-size: 13; -fx-padding: 20;");
            bidHistoryList.getChildren().add(empty);
            return;
        }

        for (BidHistoryItem item : filtered) {
            bidHistoryList.getChildren().add(buildBidHistoryCard(item));
        }
    }

    private HBox buildBidHistoryCard(BidHistoryItem item) { return BidHistoryRenderer.buildCard(item); }


    //Helpers
    private void switchTab(Tab tab, Button activeBtn) {
        if (mainTabPane != null && tab != null) {
            mainTabPane.getSelectionModel().select(tab);
        }
        setActiveNavButton(activeBtn);
    }

    private static final String NAV_ACTIVE   =
            "-fx-background-color: #0066CC; -fx-background-radius: 8; " +
            "-fx-font-size: 13; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; " +
            "-fx-text-fill: white; -fx-cursor: hand;";
    private static final String NAV_INACTIVE =
            "-fx-background-color: transparent; -fx-background-radius: 8; " +
            "-fx-font-size: 13; -fx-alignment: CENTER_LEFT; -fx-padding: 10 15; " +
            "-fx-text-fill: #CBD5E1; -fx-cursor: hand;";

    private void setActiveNavButton(Button active) {
        Button[] navButtons = {
            btnDashBoard, btnAuction, btnOrderHistory,
            btnNotification, btnProfile, btnSettings
        };
        for (Button btn : navButtons) {
            if (btn != null) {
                btn.setStyle(btn == active ? NAV_ACTIVE : NAV_INACTIVE);
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

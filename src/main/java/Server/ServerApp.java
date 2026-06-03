package Server;

import Server.controller.AuctionApiController;
import Server.controller.BidApiController;
import Server.controller.ItemApiController;
import Server.controller.NotificationApiController;
import Server.controller.UserApiController;
import Server.dao.NotificationDAO;
import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.dao.users.UserDAO;
import Server.filters.sessionFilter;
import Server.networking.DataSourceFactory;
import Server.networking.ServerConnection;
import Server.networking.http.ApiRouter;
import Server.service.NotificationService;
import Server.service.auction.AuctionExpiryScheduler;
import Server.service.auction.AuctionService;
import Server.service.auction.AutoBidConfigService;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;
import Server.websocket.BidWebSocketServer;

import com.sun.net.httpserver.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.sql.DataSource;

public class ServerApp {

    private static final Logger log = LoggerFactory.getLogger(ServerApp.class);

    public static void main(String[] args) throws Exception {
        DataSource dataSource = DataSourceFactory.getDataSource();

        int httpPort = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        ServerConnection server = ServerConnection.getInstance();
        server.init(httpPort);

        // DAOs
        UserDAO userDAO = new UserDAO(dataSource);
        ItemDAO itemDAO = new ItemDAO(dataSource);
        AuctionDAO auctionDAO = new AuctionDAO(dataSource);
        BidDAO bidDAO = new BidDAO(dataSource);

        // Services
        UserService userService = new UserService(userDAO);
        NotificationDAO notificationDAO = new NotificationDAO(dataSource);
        NotificationService notificationService = new NotificationService(notificationDAO, userDAO);
        AuctionService auctionService = new AuctionService(auctionDAO, bidDAO, itemDAO, userDAO, notificationService);
        BiddingService biddingService = new BiddingService(dataSource, userDAO, auctionDAO, bidDAO);
        AutoBidConfigService autoBidConfigService = new AutoBidConfigService(biddingService);
        biddingService.setAutoBidConfigService(autoBidConfigService);
        biddingService.setNotificationService(notificationService);
        ItemService itemService = new ItemService(itemDAO);

        // Controllers
        UserApiController userController = new UserApiController(userService);
        AuctionApiController auctionController = new AuctionApiController(auctionService, userService, itemService);
        NotificationApiController notifController = new NotificationApiController(notificationService);
        BidApiController bidController = new BidApiController(biddingService, userService, itemService);
        ItemApiController itemController = new ItemApiController(itemService);

        // Router
        ApiRouter router = new ApiRouter();

        // --- User routes (login + register are public; filter whitelists them) ---
        router.register("POST", "/api/users/login",           userController::login);
        router.register("POST", "/api/users/register",        userController::register);
        router.register("POST", "/api/users/logout",          userController::logout);
        router.register("GET",  "/api/users/me",              userController::getMe);
        router.register("POST", "/api/users/change-password", userController::changePassword);
        router.register("GET",  "/api/users/all",             userController::getAllUsers);
        router.register("POST", "/api/users/{id}/ban",        userController::banUser);
        router.register("POST", "/api/users/{id}/unban",      userController::unbanUser);
        router.register("POST", "/api/users/preferences",     userController::updatePreferences);
        router.register("POST", "/api/users/delete",          userController::deleteAccount);
        router.register("POST", "/api/users/deposit",         userController::deposit);

        // --- Auction routes ---
        router.register("POST", "/api/auctions/create",  auctionController::createAuction);
        router.register("GET",  "/api/auctions",         auctionController::getAllAuctions);
        router.register("GET",  "/api/auctions/mine",    auctionController::getMyAuctions);
        router.register("POST", "/api/auctions/get",     auctionController::getAuction);
        router.register("POST", "/api/auctions/cancel",  auctionController::cancelAuction);
        router.register("POST", "/api/auctions/refresh", auctionController::refreshStatus);

        // --- Bid routes ---
        router.register("POST", "/api/bids/place",        bidController::placeBid);
        router.register("POST", "/api/bids/history",      bidController::getBidHistory);
        router.register("POST", "/api/bids/autobid",      bidController::registerAutoBid);
        router.register("GET",  "/api/bids/my-auctions",  bidController::getMyBiddingAuctions);
        router.register("GET",  "/api/bids/my-history",   bidController::getMyBidHistory);

        // --- Item routes ---
        router.register("GET",  "/api/items",          itemController::getMyItems);
        router.register("POST", "/api/items/get",      itemController::getItem);
        router.register("POST", "/api/items/create",   itemController::createItem);
        router.register("POST", "/api/items/update",   itemController::updateItem);
        router.register("POST", "/api/items/delete",   itemController::deleteItem);
        router.register("GET", "/api/items/all", itemController::getAllItems);
        // --- Notification routes ---
        router.register("GET",  "/api/notifications",          notifController::getNotifications);
        router.register("POST", "/api/notifications/read-all", notifController::markAllRead);

        HttpContext context = server.getServer().createContext("/", router);
        context.getFilters().add(new sessionFilter(userService));

        server.start();
        log.info("HTTP server listening on port {}", httpPort);

        // --- WebSocket server for real-time bid updates ---
        BidWebSocketServer wsServer = BidWebSocketServer.getInstance();
        wsServer.start();

        // --- Background scheduler: auto-expire UPCOMING/ACTIVE auctions ---
        AuctionExpiryScheduler expiryScheduler = new AuctionExpiryScheduler(auctionDAO, auctionService);
        expiryScheduler.start();

        log.info("Routes registered: users, auctions, bids, items, notifications");
    }
}

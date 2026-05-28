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
import Server.service.auction.AuctionService;
import Server.service.auction.AutoBidConfigService;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;
import Server.websocket.BidWebSocketServer;

import com.sun.net.httpserver.HttpContext;
import Server.controller.responseObjects.OrderApiController;
import Server.dao.auction.OrderDAO;
import Server.service.auction.OrderService;
import javax.sql.DataSource;

public class ServerApp {
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
        OrderDAO orderDAO = new OrderDAO(dataSource);

        // Services
        UserService userService = new UserService(userDAO);
        NotificationDAO notificationDAO = new NotificationDAO(dataSource);
        NotificationService notificationService = new NotificationService(notificationDAO);
        AuctionService auctionService = new AuctionService(auctionDAO, bidDAO, itemDAO, userDAO, notificationService);
        BiddingService biddingService = new BiddingService(dataSource, userDAO, auctionDAO, bidDAO);
        AutoBidConfigService autoBidConfigService = new AutoBidConfigService(biddingService);
        biddingService.setAutoBidConfigService(autoBidConfigService);
        ItemService itemService = new ItemService(itemDAO);
        OrderService orderService = new OrderService(orderDAO);

        // Controllers
        UserApiController userController = new UserApiController(userService);
        AuctionApiController auctionController = new AuctionApiController(auctionService, userService);
        NotificationApiController notifController = new NotificationApiController(notificationService);
        BidApiController bidController = new BidApiController(biddingService, userService);
        ItemApiController itemController = new ItemApiController(itemService);
        OrderApiController orderController = new OrderApiController(orderService);

        // Router
        ApiRouter router = new ApiRouter();

        // --- User routes (login + register are public; filter whitelists them) ---
        router.register("POST", "/api/users/login",           userController::login);
        router.register("POST", "/api/users/register",        userController::register);
        router.register("GET",  "/api/users/me",              userController::getMe);
        router.register("POST", "/api/users/change-password", userController::changePassword);

        // --- Auction routes ---
        router.register("POST", "/api/auctions/create",  auctionController::createAuction);
        router.register("GET",  "/api/auctions",         auctionController::getAllAuctions);
        router.register("GET",  "/api/auctions/mine",    auctionController::getMyAuctions);
        router.register("POST", "/api/auctions/get",     auctionController::getAuction);
        router.register("POST", "/api/auctions/cancel",  auctionController::cancelAuction);
        router.register("POST", "/api/auctions/refresh", auctionController::refreshStatus);

        // --- Bid routes ---
        router.register("POST", "/api/bids/place",   bidController::placeBid);
        router.register("POST", "/api/bids/history", bidController::getBidHistory);
        router.register("POST", "/api/bids/autobid", bidController::registerAutoBid);

        // --- Item routes ---
        router.register("GET",  "/api/items",          itemController::getMyItems);
        router.register("POST", "/api/items/get",      itemController::getItem);
        router.register("POST", "/api/items/create",   itemController::createItem);
        router.register("POST", "/api/items/update",   itemController::updateItem);
        router.register("POST", "/api/items/delete",   itemController::deleteItem);

        // --- Order routes ---
        router.register("GET", "/api/orders/recent", orderController::getRecentOrders);
        router.register("GET", "/api/orders/all",    orderController::getAllOrders);

        // --- Notification routes ---
        router.register("GET",  "/api/notifications",          notifController::getNotifications);
        router.register("POST", "/api/notifications/read-all", notifController::markAllRead);

        HttpContext context = server.getServer().createContext("/", router);
        context.getFilters().add(new sessionFilter(userService));

        server.start();
        System.out.println("[Server] HTTP server listening on :" + httpPort);

        // --- WebSocket server for real-time bid updates ---
        BidWebSocketServer wsServer = BidWebSocketServer.getInstance();
        wsServer.start();

        System.out.println("[Server] Routes registered: users, auctions, bids, items, orders");
    }
}

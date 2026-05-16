package Server;

import Server.controller.AuctionApiController;
import Server.controller.BidApiController;
import Server.controller.ItemApiController;
import Server.controller.UserApiController;
import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.dao.users.UserDAO;
import Server.filters.sessionFilter;
import Server.networking.DataSourceFactory;
import Server.networking.ServerConnection;
import Server.networking.http.ApiRouter;
import Server.service.auction.AuctionService;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

import com.sun.net.httpserver.HttpContext;

import javax.sql.DataSource;

public class ServerApp {
    public static void main(String[] args) throws Exception {
        DataSource dataSource = DataSourceFactory.getDataSource();

        ServerConnection server = ServerConnection.getInstance();
        server.init(8080);

        // DAOs
        UserDAO userDAO = new UserDAO(dataSource);
        ItemDAO itemDAO = new ItemDAO(dataSource);
        AuctionDAO auctionDAO = new AuctionDAO(dataSource);
        BidDAO bidDAO = new BidDAO(dataSource);

        // Services
        UserService userService = new UserService(userDAO);
        AuctionService auctionService = new AuctionService(auctionDAO, bidDAO, itemDAO);
        BiddingService biddingService = new BiddingService(dataSource, userDAO, auctionDAO, bidDAO);
        ItemService itemService = new ItemService(itemDAO);

        // Controllers
        UserApiController userController = new UserApiController(userService);
        AuctionApiController auctionController = new AuctionApiController(auctionService);
        BidApiController bidController = new BidApiController(biddingService);
        ItemApiController itemController = new ItemApiController(itemService);

        // Router
        ApiRouter router = new ApiRouter();

        // --- User routes (login + register are public; filter whitelists them) ---
        router.register("POST", "/api/users/login",    userController::login);
        router.register("POST", "/api/users/register", userController::register);
        router.register("GET",  "/api/users/me",        userController::getMe);

        // --- Auction routes ---
        router.register("POST", "/api/auctions/create",  auctionController::createAuction);
        router.register("GET",  "/api/auctions",         auctionController::getAllAuctions);
        router.register("POST", "/api/auctions/get",     auctionController::getAuction);
        router.register("POST", "/api/auctions/cancel",  auctionController::cancelAuction);
        router.register("POST", "/api/auctions/refresh", auctionController::refreshStatus);

        // --- Bid routes ---
        router.register("POST", "/api/bids/place",   bidController::placeBid);
        router.register("POST", "/api/bids/history", bidController::getBidHistory);

        // --- Item routes ---
        router.register("GET",  "/api/items",          itemController::getMyItems);
        router.register("POST", "/api/items/get",      itemController::getItem);
        router.register("POST", "/api/items/create",   itemController::createItem);
        router.register("POST", "/api/items/update",   itemController::updateItem);
        router.register("POST", "/api/items/delete",   itemController::deleteItem);

        // Apply auth filter to every request (login/register are whitelisted inside the filter)
        HttpContext context = server.getServer().createContext("/", router);
        context.getFilters().add(new sessionFilter(userService));

        server.start();

        System.out.println("[Server] Listening on :8080");
        System.out.println("[Server] Routes registered: users, auctions, bids, items");
    }
}
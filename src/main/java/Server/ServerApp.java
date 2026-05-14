package Server;


import Server.controller.UserApiController;
import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.dao.users.UserDAO;
import Server.networking.DataSourceFactory;
import Server.networking.ServerConnection;
import Server.networking.http.ApiRouter;
import Server.service.users.UserService;

import javax.sql.DataSource;
public class ServerApp {
    public static void main(String[] args)  throws Exception {
        DataSource dataSource = DataSourceFactory.getDataSource();

        ServerConnection server = ServerConnection.getInstance();

        server.init(8080);

        //* DAOS

        UserDAO userDAO =
                new UserDAO(dataSource);

        ItemDAO itemDAO =
                new ItemDAO(dataSource);

        AuctionDAO auctionDAO =
                new AuctionDAO(dataSource);

        BidDAO bidDAO =
                new BidDAO(dataSource);

        //SERVICES

        UserService userService = new UserService(userDAO);

        // ROUTER

        ApiRouter router = new ApiRouter();

        UserApiController userController =
                new UserApiController(userService);

        router.register(
                "POST",
                "/api/users/login",
                userController::login
        );

        router.register(
                "POST",
                "/api/users/register",
                userController::register
        );

        router.register(
                "POST",
                "/api/users/loginwtoken",
                userController::loginWithToken
        );

        server.getServer().createContext("/", router);

        server.start();

        System.out.println("registered");
    }
}
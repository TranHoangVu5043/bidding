package Server;

import Server.networking.ServerConnection;

public class ServerApp {
    public static void main(String[] args)  throws Exception {
        ServerConnection server = ServerConnection.getInstance();

        server.init(8080);
        server.start();
    }
}
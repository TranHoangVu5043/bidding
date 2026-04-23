package Server.networking;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.io.IOException;

public class ServerConnection {

    private static ServerConnection instance;
    private HttpServer server;

    private ServerConnection() {}

    public static synchronized ServerConnection getInstance() {
        if (instance == null) {
            instance = new ServerConnection();
        }
        return instance;
    }

    public void init(int port) throws IOException {
        if (server != null) return; // prevent re-init

        server = HttpServer.create(new InetSocketAddress(port), 0);


        server.setExecutor(null);
    }

    public void start() {
        if (server == null) {
            throw new IllegalStateException("Server not initialized!");
        }
        server.start();
        System.out.println("Server started");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("Server stopped");
        }
    }

    public HttpServer getServer() {
        return server;
    }
}
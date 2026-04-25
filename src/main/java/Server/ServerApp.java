package Server;

import Server.networking.Database;
import Server.networking.ServerConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ServerApp {
    public static void main(String[] args)  throws Exception {
        System.setProperty("java.net.preferIPv4Stack", "true");

        ServerConnection server = ServerConnection.getInstance();

        server.init(8080);
        server.start();

        try (Connection conn = Database.getInstance().getConnection()) {

            if (conn == null || conn.isClosed()) {
                System.out.println("Connection failed");
                return;
            }

            System.out.println("Connected successfully!");

            // 2. Validate connection
            if (conn.isValid(3)) {
                System.out.println("Connection is valid");
            } else {
                System.out.println("Connection is NOT valid");
            }

            // 3. Run test query
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT 1")) {

                if (rs.next()) {
                    System.out.println("Query executed successfully!");
                }
            }

        }
    }
}
package Server;


import Server.networking.DataSourceFactory;
import Server.networking.ServerConnection;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ServerApp {
    public static void main(String[] args)  throws Exception {
        DataSource ds = DataSourceFactory.getDataSource();


        ServerConnection server = ServerConnection.getInstance();

        server.init(8080);
        server.start();

        /*try (Connection conn = ds.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                System.out.println("DB responded: " + rs.getInt(1));
            }
        }*/
    }
}
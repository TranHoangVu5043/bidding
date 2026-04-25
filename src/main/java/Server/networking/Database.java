package Server.networking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    private static Database instance;

    private final String url;
    private final String user;
    private final String password;

    private Database() {
        //TODO: Do this when hosted on railway / render
        //this.url = System.getenv("DB_URL");
        //this.user = System.getenv("DB_USER");
        //this.password = System.getenv("DB_PASS");

        this.url = "jdbc:postgresql://db.kxxbzrejmsoxjtpnjpdo.supabase.co:5432/postgres?sslmode=require";
        this.user = "postgres";
        this.password = "LTNC_CS5#1234";
    }

    public static synchronized Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
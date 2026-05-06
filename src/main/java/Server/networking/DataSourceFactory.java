package Server.networking;

import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

public class DataSourceFactory {

    private static final PGSimpleDataSource dataSource;

    static {
        dataSource = new PGSimpleDataSource();

        dataSource.setServerNames(new String[]{"postgres"});
        dataSource.setPortNumbers(new int[]{5432});
        dataSource.setDatabaseName("postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("LTNC_CS5#1234");

        dataSource.setCurrentSchema("public");
        dataSource.setApplicationName("BiddingApp");
    }

    private DataSourceFactory() {} // prevent instantiation

    public static DataSource getDataSource() {
        return dataSource;
    }
}
/*
package Server.networking;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatasourceFactory {
    private static DatasourceFactory instance;

    private final String url;
    private final String user;
    private final String password;

    private DatasourceFactory() {
        //TODO: Do this when hosted on railway / render
        //this.url = System.getenv("DB_URL");
        //this.user = System.getenv("DB_USER");
        //this.password = System.getenv("DB_PASS");

        this.url = "jdbc:postgresql://db.kxxbzrejmsoxjtpnjpdo.supabase.co:5432/postgres?sslmode=require";
        this.user = "postgres";
        this.password = "LTNC_CS5#1234";
    }

    public static synchronized DatasourceFactory getInstance() {
        if (instance == null) {
            instance = new DatasourceFactory();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {


        return DriverManager.getConnection(url, user, password);
    }
}
 */
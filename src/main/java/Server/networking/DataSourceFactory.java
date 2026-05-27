package Server.networking;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {

    private static final HikariDataSource pool;

    static {
        HikariConfig cfg = new HikariConfig();

        cfg.setJdbcUrl("jdbc:postgresql://db.kxxbzrejmsoxjtpnjpdo.supabase.co:5432/postgres?sslmode=require");
        cfg.setUsername("postgres");
        cfg.setPassword("LTNC_CS5#1234");
        cfg.setSchema("public");
        cfg.setDriverClassName("org.postgresql.Driver");

        // ── Pool sizing ──────────────────────────────────────────────────
        cfg.setMinimumIdle(2);       // keep 2 warm connections alive at all times
        cfg.setMaximumPoolSize(10);  // max 10 concurrent connections

        // ── Timeouts ─────────────────────────────────────────────────────
        cfg.setConnectionTimeout(30_000);  // max wait to borrow a connection: 30 s
        cfg.setIdleTimeout(600_000);       // retire idle connections after 10 min
        cfg.setMaxLifetime(1_800_000);     // recycle connections after 30 min
        cfg.setKeepaliveTime(60_000);      // ping the DB every 60 s to prevent stale conns

        pool = new HikariDataSource(cfg);
        System.out.println("[DB] HikariCP pool started  (minIdle=2, maxPool=10)");
    }

    private DataSourceFactory() {}

    public static DataSource getDataSource() {
        // TimedDataSource wraps the pool so benchmark logs still appear,
        // but now getConnection() should show ~1–5 ms instead of ~600 ms.
        return new TimedDataSource(pool);
    }
}

package Server.dao.auction;

import Server.model.auction.items.Art;
import Server.model.auction.items.Item;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemDAOTest {
    private JdbcDataSource dataSource;
    private Connection conn;
    private ItemDAO itemDAO;

    @BeforeEach
    public void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");

        conn = dataSource.getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS items (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255), " +
                    "description TEXT, " +
                    "owner_id INT, " +
                    "category VARCHAR(100), " +
                    "condition VARCHAR(100), " +
                    "price DOUBLE, " +
                    "stock INT" +
                    ")");
        }

        itemDAO = new ItemDAO(dataSource);
    }
    @AfterEach
    public void tearDown() throws Exception {
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS items");
            }
            conn.close();
        }
    }
    @Test
    public void testCreateItem_Success() throws SQLException{
        Item newitem = new Art(0, "Tranh","Đẹp",2,"chat","Ổn",1000,12);
        Item item = itemDAO.create(newitem);

        assertNotNull(item, "Tạo thành công");
        assertTrue(item.getId() > 0, "ID phải tự tăng");
        assertEquals("Tranh", item.getName());
    }
    @Test
    public void testFindByOwnerIdSuccess() throws SQLException {
        try(Statement stmt = conn.createStatement()) {
            String sql1 = "INSERT INTO items (id, name, description, owner_id, category, condition, price, stock)" +
                    "VALUES (1, 'Tranh', 'Đẹp', 100, '100', '100', 120, 12)";
            stmt.execute(sql1);
        }
        List<Item> item = itemDAO.findByOwnerId(100);
        assertNotNull(item);
        assertEquals(1, item.size());
        assertEquals("Tranh", item.get(0).getName());
    }
}

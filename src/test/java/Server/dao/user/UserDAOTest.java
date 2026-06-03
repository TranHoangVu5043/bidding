package Server.dao.user;

import Server.dao.users.UserDAO;
import Server.model.users.Bidder;
import Server.model.users.User;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {
    private JdbcDataSource dataSource;
    private Connection conn;
    private UserDAO userDAO;

    @BeforeEach
    public void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        conn = dataSource.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(255), " +
                    "password VARCHAR(255), " +
                    "email VARCHAR(255), " +
                    "role VARCHAR(50), " +
                    "balance DOUBLE, " +
                    "store_name VARCHAR(255), " +
                    "status VARCHAR(50) DEFAULT 'ACTIVE'" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS user_settings (" +
                    "user_id INT PRIMARY KEY, " +
                    "notif_auction BOOLEAN DEFAULT TRUE, " +
                    "notif_email BOOLEAN DEFAULT FALSE" +
                    ")");
        }
        userDAO = new UserDAO(dataSource);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Dọn dẹp bảng ảo sau khi test xong để không ảnh hưởng bài test khác
        if (conn != null && !conn.isClosed()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("DROP TABLE IF EXISTS users");
                stmt.execute("DROP TABLE IF EXISTS user_settings");
            }
            conn.close();
        }
    }

    @Test
    public void testFindById() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, password, email, role, balance, store_name, status) " +
                    "VALUES (1, 'huyenmy', 'ha', 'm@g.c', 'BIDDER', 100.0, null, 'ACTIVE')");
        }
        User user = userDAO.findById(conn, 1);
        assertNotNull(user, "Tài khoản tồn tại");
        assertEquals(user.getBalance(), 100, "Phải bằng nhau");
    }

    @Test
    public void testCreateUser() throws Exception {
        User user = new Bidder(1, "h", "1234", "h@g.c", 120);
        int id = userDAO.createUser(user);
        assertTrue(id > 0, "Id lớn hơn 0");
        try (Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE id = " + id)) {
            // Kiểm tra xem đã ghi vào bảng chưa
            assertTrue(rs.next(), "Phải tìm thấy người dùng vừa được tạo trong bảng users");
            // Xác minh xem các thông tin
            assertEquals("h", rs.getString("username"));
            assertEquals("1234", rs.getString("password"), "Mật khẩu phải khớp");
            assertEquals("h@g.c", rs.getString("email"), "Email phải khớp");
            assertEquals(120, rs.getDouble("balance"), "Số dư tài khoản phải là 120");
        }

    }
    @Test
    public void testUpdateBalance_Success() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, balance) VALUES (2, 'b', 500)");
        }
        userDAO.updateBalance(conn, 2, 300);
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT balance FROM users WHERE id = 2")) {
            assertTrue(rs.next());
            assertEquals(300, rs.getDouble("balance"), "Số dư cập nhật về 300 ");
        }
    }
    @Test
    public void testAddBalance_Success() throws Exception {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, balance) VALUES (3, 'u', 100.0)");
        }

        userDAO.addBalance(3, 50);
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT balance FROM users WHERE id = 3")) {
            assertTrue(rs.next());
            assertEquals(150, rs.getDouble("balance"), "Hoàn tiền 150");
        }
    }
    @Test
    public void testUpdateStatus() throws Exception{
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (id, username, balance, status) VALUES (5, 'tr', 0.0, 'ACTIVE')");
        }
        userDAO.updateStatus(5, "BANNED");
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT status FROM users WHERE id = 5")) {
            assertTrue(rs.next());
            assertEquals("BANNED", rs.getString("status"), "Trạng thái của User trong DB phải chuyển thành BANNED");
        }
    }
    @Test
    public void testUpdatePassWord() throws Exception{
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO users (id, username,password, balance, status) VALUES (5, 'tr', 'h',  0.0, 'ACTIVE')");
        }
        userDAO.updatePassword(5, "he");
        try (Statement stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT password FROM users WHERE id = 5")) {
            assertTrue(rs.next());
            assertEquals("he", rs.getString("password"), "Thay đổi pass");
        }
    }
}

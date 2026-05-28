package Client.model.user;

public class User {

    private int id;

    private String username;
    private String password;
    private String email;

    private String role;

    private double balance;

    private String  storeName;
    private String  createdAt;
    private String  updatedAt;
    private boolean notifAuction = true;
    private boolean notifEmail   = false;

    public User() {
    }

    public User(
            int id,
            String username,
            String password,
            String email,
            String role,
            double balance,
            String createdAt,
            String updatedAt
    ) {

        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.balance = balance;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isNotifAuction() { return notifAuction; }
    public void setNotifAuction(boolean notifAuction) { this.notifAuction = notifAuction; }

    public boolean isNotifEmail() { return notifEmail; }
    public void setNotifEmail(boolean notifEmail) { this.notifEmail = notifEmail; }
}

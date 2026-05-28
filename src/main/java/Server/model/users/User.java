package Server.model.users;

import Server.model.Entity;

public abstract class User implements Entity {

    private int id;
    private String username;
    private String password;
    private String email;
    private String role;
    private double balance;
    private String status;
    private double  balance;
    private boolean notifAuction = true;
    private boolean notifEmail   = false;

    public User(int id, String username, String password, String email, String role, double balance) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.balance = balance;
    }

    @Override
    public int getId() {
        return id;
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

    public double getBalance() {
        return balance;
    }

    public boolean isNotifAuction() { return notifAuction; }
    public void setNotifAuction(boolean notifAuction) { this.notifAuction = notifAuction; }

    public boolean isNotifEmail() { return notifEmail; }
    public void setNotifEmail(boolean notifEmail) { this.notifEmail = notifEmail; }

    @Override
    public abstract void displayInfo();
}

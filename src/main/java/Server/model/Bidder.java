package Server.model;

public class Bidder extends User{
    private double balance;
    public Bidder(String id, String username, String password, String email) {
        super(id, username, password, email, "BIDDER");
        this.balance = 0.0;
    }
    @Override
    public void displayInfo() {
        System.out.println("[Bidder] Username: " + getUsername() + ", Email: " + getEmail());
    }
}

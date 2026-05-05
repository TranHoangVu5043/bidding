package Server.model;

public class Bidder extends User{

    public Bidder(int id, String username, String password, String email, double balance) {
        super(id, username, password, email, "BIDDER", balance);
    }
    @Override
    public void displayInfo() {
        System.out.println("[Bidder] Username: " + getUsername() + ", Email: " + getEmail());
    }
}

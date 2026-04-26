package Server.model;

public class Seller extends User {
    private String storeName;
    public Seller(String id, String username, String password, String email, String storeName) {
        super(id, username, password, email, "SELLER");
        this.storeName = storeName;
    }

    @Override
    public void displayInfo() {
        System.out.println("[Seller] Store: " + storeName + ", Owner: " + getUsername());
    }
}

package Server.model.users;

public class Seller extends User {
    private String storeName;
    public Seller(int id, String username, String password, String email, String storeName, double balance) {
        super(id, username, password, email, "SELLER", balance);

        this.storeName = storeName;
    }

    @Override
    public void displayInfo() {
        System.out.println("[Seller] Store: " + storeName + ", Owner: " + getUsername());
    }
}

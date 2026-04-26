package Server.model;

public class Admin extends User {

    public Admin(String id, String username, String password, String email) {
        super(id, username, password, email, "ADMIN");
    }

    /**
     * Polymorphism: Triển khai cách hiển thị riêng cho Admin[cite: 121].
     */
    @Override
    public void displayInfo() {
        System.out.println("--- System Administrator ---");
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("---------------------------");
    }
}
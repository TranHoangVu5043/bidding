package Server.model.users;

public class Admin extends User {

    public Admin(int id, String username, String password, String email, double balance) {
        super(id, username, password, email, "ADMIN", balance);
    }

    @Override
    public void displayInfo() {
        System.out.println("--- System Administrator ---");
        System.out.println("Username: " + getUsername());
        System.out.println("Email: " + getEmail());
        System.out.println("---------------------------");
    }
}
package Server.model.users;

import Server.model.users.records.UserRow;

public class UserFactory {

    private UserFactory() {}

    public static User createUser(UserRow row) {
        return switch (row.role().toUpperCase()) {
            case "BIDDER" -> new Bidder(row.id(), row.username(), row.password(), row.email(), row.balance());
            case "SELLER" -> new Seller(row.id(), row.username(), row.password(), row.email(), null, row.balance());
            case "ADMIN"  -> new Admin(row.id(), row.username(), row.password(), row.email(), row.balance());
            default -> throw new IllegalArgumentException("Unknown role: " + row.role());
        };
    }
}
package Server.model.users.records;

public record UserRow(
        int    id,
        String username,
        String password,
        String email,
        String role,
        double balance,
        String storeName
) {}
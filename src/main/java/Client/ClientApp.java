package Client;

import Client.networking.ClientConnection;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class ClientApp
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        ClientConnection client = ClientConnection.getInstance();

        // 🔹 Test GET
        System.out.println("=== GET /auctions ===");
        System.out.println(client.get("/auctions"));

        System.out.println("\n====================\n");

        // 🔹 Test LOGIN
        String loginJson = """
        {
          "username": "john",
          "password": "123"
        }
        """;

        System.out.println("=== POST /auth/login ===");
        String loginResponse = client.post("/auth/login", loginJson);
        System.out.println(loginResponse);
    }
}

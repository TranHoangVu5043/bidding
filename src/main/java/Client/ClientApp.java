package Client;

import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.UserApi;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class ClientApp
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        UserApi userApi = new UserApi();

        ApiResponse<String> response =
                userApi.login(
                        "john",
                        "123456"
                );

        if (response.getStatus() == 200) {

            System.out.println("Logged in!");

            System.out.println(
                    "Token: "
                            + SessionManager.getToken()
            );

        } else {

            System.out.println(response.getMessage());
        }
    }
}

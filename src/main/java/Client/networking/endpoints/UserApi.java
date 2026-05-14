package Client.networking.endpoints;

import Client.model.User;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class UserApi {

    private final ApiClient apiClient;
    private final Gson gson;

    public UserApi() {

        this.apiClient = new ApiClient();
        this.gson = new Gson();
    }

    // ===== LOGIN =====

    public ApiResponse<String> login(
            String username,
            String password
    ) {

        try {

            User request = new User();

            request.setUsername(username);
            request.setPassword(password);

            String responseJson =
                    apiClient.post(
                            "/users/login",
                            request
                    );

            ApiResponse<String> response =
                    gson.fromJson(
                            responseJson,
                            new TypeToken<ApiResponse<String>>() {
                            }.getType()
                    );

            if (response.getStatus() == 200) {
                SessionManager.setToken(response.getData());
            }

            return response;

        } catch (Exception e) {

            ApiResponse<String> response =
                    new ApiResponse<>();

            response.setStatus(500);
            response.setMessage(e.getMessage());

            return response;
        }
    }
    // ===== REGISTER =====

    public ApiResponse<Void> register(
            String username,
            String password,
            String email
    ) {

        try {

            User request = new User();

            request.setUsername(username);
            request.setPassword(password);
            request.setEmail(email);

            String responseJson =
                    apiClient.post(
                            "/users/register",
                            request
                    );

            return gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<Void>>() {
                    }.getType()
            );

        } catch (Exception e) {

            ApiResponse<Void> response =
                    new ApiResponse<>();

            response.setStatus(500);
            response.setMessage(e.getMessage());

            return response;
        }
    }

    // ===== AUTHENTICATE =====

    public ApiResponse<User> authenticate() {

        try {

            String responseJson =
                    apiClient.post(
                            "/users/authenticate",
                            new Object()
                    );

            return gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<User>>() {
                    }.getType()
            );

        } catch (Exception e) {

            ApiResponse<User> response =
                    new ApiResponse<>();

            response.setStatus(500);
            response.setMessage(e.getMessage());

            return response;
        }
    }
}
package Client.networking.endpoints;

import Client.model.user.User;
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
            String email,
            String role
    ) {

        try {

            User request = new User();

            request.setUsername(username);
            request.setPassword(password);
            request.setEmail(email);
            request.setRole(role);

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

    // ===== CHANGE PASSWORD =====

    public ApiResponse<Void> changePassword(String oldPassword, String newPassword) {
        try {

            String responseJson = apiClient.post(
                    "/users/change-password",
                    new ChangePasswordRequest(oldPassword, newPassword)
            );

            return gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<Void>>() {}.getType()
            );

        } catch (Exception e) {
            ApiResponse<Void> response = new ApiResponse<>();
            response.setStatus(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }

    private static class ChangePasswordRequest {
        private final String oldPassword;
        private final String newPassword;

        ChangePasswordRequest(String oldPassword, String newPassword) {
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }
    }

    // ===== GET CURRENT USER =====

    public ApiResponse<User> getMe() {

        try {

            String responseJson = apiClient.get("/users/me");

            return gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<User>>() {
                    }.getType()
            );

        } catch (Exception e) {

            ApiResponse<User> response = new ApiResponse<>();
            response.setStatus(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }
    // ===== LOGOUT =====

    public ApiResponse<Void> logout() {
        try {
            String responseJson = apiClient.post("/users/logout", null);

            ApiResponse<Void> response = gson.fromJson(
                    responseJson,
                    new TypeToken<ApiResponse<Void>>() {}.getType()
            );

            SessionManager.clear();

            return response;

        } catch (Exception e) {
            // Dù lỗi vẫn xóa token — không để user bị kẹt
            SessionManager.clear();

            ApiResponse<Void> response = new ApiResponse<>();
            response.setStatus(500);
            response.setMessage(e.getMessage());
            return response;
        }
    }
}
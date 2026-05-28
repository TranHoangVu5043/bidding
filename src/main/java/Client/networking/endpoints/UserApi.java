package Client.networking.endpoints;

import Client.dto.requests.ChangePasswordRequest;
import Client.dto.requests.DepositBody;
import Client.dto.requests.NotifPrefsBody;
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
            String role,
            String storeName
    ) {

        try {

            User request = new User();

            request.setUsername(username);
            request.setPassword(password);
            request.setEmail(email);
            request.setRole(role);
            request.setStoreName(storeName);

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

    // ===== UPDATE NOTIFICATION PREFERENCES =====

    public ApiResponse<Void> updatePreferences(boolean notifAuction, boolean notifEmail) {
        try {
            String responseJson = apiClient.post("/users/preferences",
                    new NotifPrefsBody(notifAuction, notifEmail));
            return gson.fromJson(responseJson,
                    new com.google.gson.reflect.TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            ApiResponse<Void> r = new ApiResponse<>();
            r.setStatus(500); r.setMessage(e.getMessage());
            return r;
        }
    }

    // ===== DELETE ACCOUNT =====

    public ApiResponse<Void> deleteAccount() {
        try {
            String responseJson = apiClient.post("/users/delete", null);
            return gson.fromJson(responseJson,
                    new com.google.gson.reflect.TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            ApiResponse<Void> r = new ApiResponse<>();
            r.setStatus(500); r.setMessage(e.getMessage());
            return r;
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
    // ===== DEPOSIT =====

    public ApiResponse<Double> deposit(double amount) {
        try {
            String responseJson = apiClient.post("/users/deposit", new DepositBody(amount));
            return gson.fromJson(responseJson,
                    new TypeToken<ApiResponse<Double>>() {}.getType());
        } catch (Exception e) {
            ApiResponse<Double> r = new ApiResponse<>();
            r.setStatus(500); r.setMessage(e.getMessage());
            return r;
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
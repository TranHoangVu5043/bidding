package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.model.users.User;
import Server.service.users.UserService;

import com.google.gson.Gson;

public class UserApiController {

    private final UserService userService;
    private final Gson gson;

    public UserApiController(UserService userService) {
        this.userService = userService;
        this.gson = new Gson();
    }

    // ===== LOGIN =====

    public void login(RequestWrapper req, ResponseWrapper res) {

        try {

            User loginData = gson.fromJson(req.getBody(), User.class);

            String token = userService.login(loginData.getUsername(), loginData.getPassword());

            ApiResponse<String> response = new ApiResponse<>(200, "Đăng nhập thành công!", token);

            res.sendJson(200, gson.toJson(response));

        } catch (Exception e) {

            res.error(401, "Lỗi: " + e.getMessage());
        }
    }

    // ===== REGISTER =====

    public void register(RequestWrapper req, ResponseWrapper res) {

        try {

            User newUser = gson.fromJson(req.getBody(), User.class);

            userService.register(newUser.getUsername(), newUser.getPassword(), newUser.getEmail());

            ApiResponse<String> response = new ApiResponse<>(201, "Đăng ký thành công!", null);

            res.sendJson(201, gson.toJson(response));

        } catch (Exception e) {
            res.error(401, "Loi dang ky");
        }
    }

    // ===== LOGIN WITH TOKEN =====

    public void loginWithToken(RequestWrapper req, ResponseWrapper res) {

        try {

            User loginData = gson.fromJson(req.getBody(), User.class);

            String token = userService.login(loginData.getUsername(), loginData.getPassword());

            User user = userService.authenticate(token);

            ApiResponse<User> response = new ApiResponse<>(200, "Đăng nhập thành công!", user);

            res.sendJson(200, gson.toJson(response));

        } catch (Exception e) {

            res.error(400, "Lỗi: " + e.getMessage());
        }
    }
}
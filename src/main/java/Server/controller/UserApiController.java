package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.dto.requests.UserRequestDTO;
import Server.exception.AuthException;
import Server.exception.ConflictException;
import Server.exception.ValidationException;
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

    // ===== POST /api/users/register =====

    public void register(RequestWrapper req, ResponseWrapper res) {
        try {
            UserRequestDTO body = gson.fromJson(req.getBody(), UserRequestDTO.class);

            if (body == null) {
                res.error(400, "Request body không hợp lệ hoặc bị thiếu.");
                return;
            }

            userService.register(body);

            res.sendJson(201, gson.toJson(
                    new ApiResponse<>(201, "Đăng ký thành công!", null)
            ));

        } catch (ValidationException e) {
            res.error(400, e.getMessage());

        } catch (ConflictException e) {
            res.error(409, e.getMessage());

        } catch (Exception e) {
            System.err.println("[UserApiController] register error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }

    // ===== POST /api/users/login =====

    public void login(RequestWrapper req, ResponseWrapper res) {
        try {
            UserRequestDTO body = gson.fromJson(req.getBody(), UserRequestDTO.class);

            if (body == null) {
                res.error(400, "Request body không hợp lệ hoặc bị thiếu.");
                return;
            }

            String token = userService.login(body.getUsername(), body.getPassword());

            res.sendJson(200, gson.toJson(
                    new ApiResponse<>(200, "Đăng nhập thành công!", token)
            ));

        } catch (ValidationException e) {
            res.error(400, e.getMessage());

        } catch (AuthException e) {
            res.error(401, e.getMessage());

        } catch (Exception e) {
            System.err.println("[UserApiController] login error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }
    // ===== POST /api/users/logout =====

    public void logout(RequestWrapper req, ResponseWrapper res) {
        try {
            String token = extractToken(req);

            // Always log out silently — don't reveal whether token was valid
            userService.logout(token);

            res.sendJson(200, gson.toJson(
                    new ApiResponse<>(200, "Đăng xuất thành công!", null)
            ));

        } catch (Exception e) {
            System.err.println("[UserApiController] logout error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }

    // ===== GET /api/users/me =====

    public void getMe(RequestWrapper req, ResponseWrapper res) {
        try {
            String token = extractToken(req);

            if (token == null) {
                res.error(401, "Token không hợp lệ hoặc bị thiếu.");
                return;
            }

            User user = userService.authenticate(token);

            if (user == null) {
                res.error(401, "Token đã hết hạn hoặc không tồn tại. Vui lòng đăng nhập lại.");
                return;
            }

            user.setPassword(null); // never send hash to client

            res.sendJson(200, gson.toJson(
                    new ApiResponse<>(200, "OK", user)
            ));

        } catch (Exception e) {
            System.err.println("[UserApiController] getMe error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }

    // ===== HELPER =====

    private String extractToken(RequestWrapper req) {
        String header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();
        return token.isEmpty() ? null : token;
    }
}
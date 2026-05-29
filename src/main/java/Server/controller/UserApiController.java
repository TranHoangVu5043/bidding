package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.dto.requests.DepositRequest;
import Server.dto.requests.NotifPrefsRequest;
import Server.dto.requests.UserRequestDTO;
import Server.exception.AuthException;
import Server.exception.ConflictException;
import Server.exception.ValidationException;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.model.users.User;
import Server.service.users.UserService;

import com.google.gson.Gson;

import java.util.List;

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

            if (token == null) {
                res.error(400, "Sai tên hoặc mật khẩu");
            } else {
                res.sendJson(200, gson.toJson(
                        new ApiResponse<>(200, "Đăng nhập thành công!", token)
                ));
            }

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
    public void getAllUsers(RequestWrapper req, ResponseWrapper res) {
        try {
            String token = extractToken(req);
            if (token == null) {
                res.error(401, "Token không hợp lệ hoặc bị thiếu.");
                return;
            }

            User requester = userService.authenticate(token);
            if (requester == null) {
                res.error(401, "Token đã hết hạn hoặc không tồn tại.");
                return;
            }

            // Chỉ ADMIN mới được xem danh sách tất cả user
            if (!"ADMIN".equalsIgnoreCase(requester.getRole())) {
                res.error(403, "Bạn không có quyền truy cập.");
                return;
            }

            List<User> users = userService.getAllUsers();
            users.forEach(u -> u.setPassword(null));

            res.sendJson(200, gson.toJson(
                    new ApiResponse<>(200, "OK", users)
            ));

        } catch (Exception e) {
            System.err.println("[UserApiController] getAllUsers error: " + e.getMessage());
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

    // ===== POST /api/users/change-password =====

    public void changePassword(RequestWrapper req, ResponseWrapper res) {
        try {
            User currentUser = req.getUser();
            if (currentUser == null) {
                res.error(401, "Chưa xác thực.");
                return;
            }

            UserRequestDTO body = gson.fromJson(req.getBody(), UserRequestDTO.class);
            if (body == null || body.getOldPassword() == null || body.getNewPassword() == null) {
                res.error(400, "Vui lòng cung cấp mật khẩu cũ và mật khẩu mới.");
                return;
            }

            boolean changed = userService.changePassword(
                    currentUser,
                    body.getOldPassword(),
                    body.getNewPassword()
            );

            if (changed) {
                res.sendJson(200, gson.toJson(
                        new ApiResponse<>(200, "Đổi mật khẩu thành công!", null)
                ));
            } else {
                res.error(401, "Mật khẩu cũ không đúng.");
            }

        } catch (IllegalArgumentException e) {
            res.error(400, e.getMessage());

        } catch (Exception e) {
            System.err.println("[UserApiController] changePassword error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }
    // ===== POST /api/users/{id}/ban =====

    public void banUser(RequestWrapper req, ResponseWrapper res) {
        try {
            User requester = req.getUser();
            if (requester == null) { res.error(401, "Chưa xác thực."); return; }
            if (!"ADMIN".equalsIgnoreCase(requester.getRole())) { res.error(403, "Bạn không có quyền thực hiện."); return; }

            int targetId = extractIdFromPath(req.getPath());
            if (targetId < 0) { res.error(400, "ID người dùng không hợp lệ."); return; }

            User target = userService.getUserById(targetId);
            if (target == null) { res.error(404, "Không tìm thấy người dùng."); return; }
            if ("ADMIN".equalsIgnoreCase(target.getRole())) { res.error(403, "Không thể khóa tài khoản Admin."); return; }

            userService.setUserStatus(targetId, "BANNED");
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Đã khóa tài khoản thành công.", null)));

        } catch (Exception e) {
            System.err.println("[UserApiController] banUser error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }

    // ===== POST /api/users/{id}/unban =====

    public void unbanUser(RequestWrapper req, ResponseWrapper res) {
        try {
            User requester = req.getUser();
            if (requester == null) { res.error(401, "Chưa xác thực."); return; }
            if (!"ADMIN".equalsIgnoreCase(requester.getRole())) { res.error(403, "Bạn không có quyền thực hiện."); return; }

            int targetId = extractIdFromPath(req.getPath());
            if (targetId < 0) { res.error(400, "ID người dùng không hợp lệ."); return; }

            User target = userService.getUserById(targetId);
            if (target == null) { res.error(404, "Không tìm thấy người dùng."); return; }

            userService.setUserStatus(targetId, "ACTIVE");
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Đã mở khóa tài khoản thành công.", null)));

        } catch (Exception e) {
            System.err.println("[UserApiController] unbanUser error: " + e.getMessage());
            res.error(500, "Lỗi hệ thống, vui lòng thử lại sau.");
        }
    }
    // Lấy ID từ path dạng /api/users/{id}/ban
    private int extractIdFromPath(String path) {
        try {
            String[] parts = path.split("/");
            for (int i = 0; i < parts.length; i++) {
                if (parts[i].equals("users") && i + 1 < parts.length) {
                    return Integer.parseInt(parts[i + 1]);
                }
            }
        } catch (NumberFormatException ignored) {}
        return -1;
    }

    // ===== POST /api/users/preferences =====

    public void updatePreferences(RequestWrapper req, ResponseWrapper res) {
        try {
            User currentUser = req.getUser();
            if (currentUser == null) { res.error(401, "Chưa xác thực."); return; }

            NotifPrefsRequest body = gson.fromJson(req.getBody(), NotifPrefsRequest.class);
            if (body == null) { res.error(400, "Dữ liệu không hợp lệ."); return; }

            userService.updateNotifPrefs(currentUser.getId(), body.notifAuction, body.notifEmail);

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Cập nhật tùy chọn thành công!", null)));
        } catch (Exception e) {
            res.error(500, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ===== POST /api/users/delete =====

    public void deleteAccount(RequestWrapper req, ResponseWrapper res) {
        try {
            User currentUser = req.getUser();
            if (currentUser == null) { res.error(401, "Chưa xác thực."); return; }

            userService.deleteAccount(currentUser.getId());

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Tài khoản đã được xóa.", null)));
        } catch (Exception e) {
            res.error(500, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ===== POST /api/users/deposit =====

    public void deposit(RequestWrapper req, ResponseWrapper res) {
        try {
            User currentUser = req.getUser();
            if (currentUser == null) { res.error(401, "Chưa xác thực."); return; }

            DepositRequest body = gson.fromJson(req.getBody(), DepositRequest.class);
            if (body == null || body.amount <= 0) {
                res.error(400, "Số tiền nạp không hợp lệ.");
                return;
            }

            double newBalance = userService.deposit(currentUser.getId(), body.amount);
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Nạp tiền thành công!", newBalance)));

        } catch (IllegalArgumentException e) {
            res.error(400, e.getMessage());
        } catch (Exception e) {
            System.err.println("[UserApiController] deposit error: " + e.getMessage());
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
package Server.controller;

import Server.dto.requests.UserRequestDTO;
import Server.exception.AuthException;
import Server.exception.ConflictException;
import Server.exception.ValidationException;
import Server.model.users.Bidder;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.users.UserService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserApiControllerTest {
    private UserApiController userApiController;

    @Mock private UserService userService;
    @Mock private RequestWrapper requestWrapper;
    @Mock private ResponseWrapper responseWrapper;

    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        userApiController = new UserApiController(userService);
    }
    @Test
    void register_NullBody_Returns400() throws Exception{
        when(requestWrapper.getBody()).thenReturn(null);
        userApiController.register(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Request body không hợp lệ hoặc bị thiếu.");
    }

    @Test
    void register_ValidationException_Returns400() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"username\":\"abc\"}");
        doThrow(new ValidationException("Mật khẩu phải từ 6 ký tự trở lên."))
                .when(userService).register(any(UserRequestDTO.class));
        userApiController.register(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Mật khẩu phải từ 6 ký tự trở lên.");
    }

    @Test
    void register_ConflictException_Returns409() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"username\":\"existed_user\"}");
        doThrow(new ConflictException("Tên đăng nhập đã tồn tại."))
                .when(userService).register(any(UserRequestDTO.class));

        userApiController.register(requestWrapper, responseWrapper);
        verify(responseWrapper).error(409, "Tên đăng nhập đã tồn tại.");
    }

    @Test
    void register_Success_Returns201() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"username\":\"new_user\",\"password\":\"123456\"}");
        userApiController.register(requestWrapper, responseWrapper);

        verify(userService).register(any(UserRequestDTO.class));
        verify(responseWrapper).sendJson(eq(201), anyString());
    }

    @Test
    void login_WrongCredentials_Returns400() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"username\":\"user\",\"password\":\"wrong\"}");
        when(userService.login("user", "wrong")).thenReturn(null);
        userApiController.login(requestWrapper, responseWrapper);
        verify(responseWrapper).error(400, "Sai tên hoặc mật khẩu");
    }

    @Test
    void login_AuthException_Returns401() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"username\":\"banned_user\",\"password\":\"123\"}");
        doThrow(new AuthException("Tài khoản của bạn đã bị khóa."))
                .when(userService).login("banned_user", "123");

        userApiController.login(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Tài khoản của bạn đã bị khóa.");
    }

    @Test
    void login_Success_Returns200WithToken() throws Exception{
        when(requestWrapper.getBody()).thenReturn("{\"username\":\"my_user\",\"password\":\"123\"}");
        when(userService.login("my_user", "123")).thenReturn("mock-jwt-token-123");

        userApiController.login(requestWrapper, responseWrapper);

        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void logout_Always_Returns200() {
        when(requestWrapper.getHeader("Authorization")).thenReturn("Bearer my-valid-token");
        userApiController.logout(requestWrapper, responseWrapper);

        verify(userService).logout("my-valid-token");
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void getMe_MissingHeader_Returns401() {
        when(requestWrapper.getHeader("Authorization")).thenReturn(null);
        userApiController.getMe(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Token không hợp lệ hoặc bị thiếu.");
    }

    @Test
    void getMe_TokenExpired_Returns401() {
        when(requestWrapper.getHeader("Authorization")).thenReturn("Bearer expired-token");
        when(userService.authenticate("expired-token")).thenReturn(null);
        userApiController.getMe(requestWrapper, responseWrapper);

        verify(responseWrapper).error(401, "Token đã hết hạn hoặc không tồn tại. Vui lòng đăng nhập lại.");
    }

    @Test
    void getMe_Success_Returns200WithUserHiddenPassword() {
        when(requestWrapper.getHeader("Authorization")).thenReturn("Bearer safe-token");

        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        mockUser.setPassword("super-secret-hash");
        when(userService.authenticate("safe-token")).thenReturn(mockUser);
        userApiController.getMe(requestWrapper, responseWrapper);
        assertNull(mockUser.getPassword());
        verify(responseWrapper).sendJson(eq(200), anyString());
    }

    @Test
    void changePassword_NotAuthenticated_Returns401() {
        when(requestWrapper.getUser()).thenReturn(null);
        userApiController.changePassword(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Chưa xác thực.");
    }

    @Test
    void changePassword_WrongOldPassword_Returns401() throws Exception {
        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(mockUser);
        when(requestWrapper.getBody()).thenReturn("{\"oldPassword\":\"wrong_pass\",\"newPassword\":\"new_pass\"}");

        when(userService.changePassword(mockUser, "wrong_pass", "new_pass")).thenReturn(false);
        userApiController.changePassword(requestWrapper, responseWrapper);
        verify(responseWrapper).error(401, "Mật khẩu cũ không đúng.");
    }
    @Test
    void changePassword_Success_Returns200() throws Exception {
        User mockUser = new Bidder(1, "john", "123", "q@g.c", 1000 );
        when(requestWrapper.getUser()).thenReturn(mockUser);
        when(requestWrapper.getBody()).thenReturn("{\"oldPassword\":\"correct_pass\",\"newPassword\":\"new_pass\"}");
        when(userService.changePassword(mockUser, "correct_pass", "new_pass")).thenReturn(true);
        userApiController.changePassword(requestWrapper, responseWrapper);
        verify(responseWrapper).sendJson(eq(200), anyString());
    }
}
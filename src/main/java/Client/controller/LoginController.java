import Client.controller.SceneUtil;
import Client.networking.NetworkClient;
import Server.controller.UserApiController;
import Server.controller.responseObjects.ApiResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static Client.controller.SceneUtil.showAlert;
import static Client.controller.SceneUtil.switchToScene;

public class LoginController {
    @FXML
    private Button btnLogin;
    @FXML
    private PasswordField txtPassWord;
    @FXML
    private TextField txtUserName;

    public static class LoginRequest {
        String username;
        String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    @FXML
    void handleLogin(ActionEvent event) {
        String user = txtUserName.getText();
        String pass = txtPassWord.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            SceneUtil.showAlert("Thông báo ", "Vui lòng nập đầy đủ thông tin");
            return;
        }
        try {
            LoginRequest data = new LoginRequest(user, pass);
            ApiResponse res = NetworkClient.getInstance().post("/login", data);
            if ( res !=null && res.getStatus() == 200) {
                Object userObj = res.getData();
                if (userObj != null) {
                    String userData = userObj.toString().toUpperCase();
                    if (userData.contains("ADMIN")) {
                        SceneUtil.switchToScene("/Client/views/Admin.fxml", "Quản Trị Viên");
                    } else if (userData.contains("SELLER")) {
                        SceneUtil.switchToScene("/Client/views/Sellerview.fxml", "Người Bán");
                    } else {
                        SceneUtil.switchToScene("/Client/views/Userview.fxml", "Người Dùng");
                    }
                }else {
                    String msg = (res != null) ? res.getMessage() : "Lỗi kết nối";
                    showAlert("Đăng nhập thất bạn ", msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SceneUtil.showAlert("Lỗi kết nối ", "Không thể kết nối tới Server");
        }
    }
}


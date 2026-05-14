package Client.controller;
import Client.controller.SceneUtil;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import Server.controller.UserApiController;
import com.google.gson.Gson;
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
            ApiClient client = new ApiClient();
            String responseJson = client.post("login",data);
            Gson gson = new Gson();
            ApiResponse res = gson.fromJson(responseJson,ApiResponse.class);
            if ( res !=null && res.getStatus() == 200) {
                Object userObj = res.getData();
                if (userObj != null) {
                    String userData = userObj.toString().toUpperCase();
                    if (userData.contains("ADMIN")) {
                        SceneUtil.switchToScene(btnLogin,"/Client/views/Adminview.fxml", "Quản Trị Viên");
                    } else if (userData.contains("SELLER")) {
                        SceneUtil.switchToScene(btnLogin,"/Client/views/Sellerview.fxml", "Người Bán");
                    } else {
                        SceneUtil.switchToScene(btnLogin,"/Client/views/Userview.fxml", "Người Dùng");
                    }
                }else {
                    String msg = (res != null) ? res.getMessage() : "Lỗi kết nối";
                    SceneUtil.showAlert("Đăng nhập thất bạn ", msg);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            SceneUtil.showAlert("Lỗi kết nối ", "Không thể kết nối tới Server");
        }
    }
}


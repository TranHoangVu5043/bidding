package Client.controller;

import Client.model.User;
import Client.networking.ApiResponse;
import Client.networking.endpoints.UserApi;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;

public class LoginController {

    @FXML private Button        btnLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtUserName;

    // JavaFX creates controllers via FXML — no constructor args.
    // Each controller owns its API objects directly.
    private final UserApi userApi = new UserApi();

    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtUserName.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        ApiResponse<String> response = userApi.login(username, password);

        if (response.getStatus() == 200) {
            // Fetch the logged-in user so we can navigate to the right view
            ApiResponse<User> meResponse = userApi.getMe();

            if (meResponse.getStatus() == 200) {
                String role = meResponse.getData().getRole();
                switch (role.toUpperCase()) {
                    case "ADMIN"  -> navigateTo("Adminview.fxml", event);
                    case "SELLER" -> navigateTo("SellerView.fxml", event);
                    default       -> navigateTo("UserView.fxml", event);
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin người dùng.");
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tài khoản hoặc mật khẩu không đúng.");
        }
    }

    // Called when Enter is pressed in the username or password field
    @FXML void showUserName(ActionEvent event) { handleLogin(event); }
    @FXML void showPassWord(ActionEvent event) { handleLogin(event); }

    private void navigateTo(String fxmlFile, ActionEvent event) {
        try {
            File fxml = new File("src/main/java/Client/views/" + fxmlFile);
            Parent root = FXMLLoader.load(fxml.toURI().toURL());
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi giao diện", "Không thể tải màn hình: " + fxmlFile);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
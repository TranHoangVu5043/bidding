package Client.controller.auth;

import Client.model.user.User;
import Client.networking.ApiResponse;
import Client.networking.SessionManager;
import Client.networking.endpoints.UserApi;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML private Button        btnLogin;
    @FXML private PasswordField txtPassword;
    @FXML private TextField     txtUserName;

    private final UserApi userApi = new UserApi();

    @FXML
    void handleLogin(ActionEvent event) {
        String username = txtUserName.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        // Disable button and show feedback so the user knows we're working
        btnLogin.setDisable(true);
        btnLogin.setText("Đang đăng nhập...");

        // Run network calls on a background thread — never block the FX thread
        new Thread(() -> {

            // Step 1: authenticate and receive token
            ApiResponse<String> loginResponse = userApi.login(username, password);

            if (loginResponse == null || loginResponse.getStatus() != 200) {
                String msg = loginResponse != null ? loginResponse.getMessage() : "Mất kết nối tới Server";
                Platform.runLater(() -> {
                    resetButton();
                    showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", msg);
                });
                return;
            }

            // Step 2: fetch logged-in user to determine role
            ApiResponse<User> meResponse = userApi.getMe();

            if (meResponse == null || meResponse.getData() == null) {
                Platform.runLater(() -> {
                    resetButton();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể lấy thông tin người dùng.");
                });
                return;
            }

            // Save user globally so every controller can read it without extra API calls
            SessionManager.setCurrentUser(meResponse.getData());

            // Step 3: back on FX thread — navigate to role-appropriate view
            String role = meResponse.getData().getRole();
            final String fxmlFile;
            final String title;
            if (role == null) role = "";
            switch (role.toLowerCase()) {
                case "admin"  -> { fxmlFile = "Adminview.fxml";  title = "Admin Dashboard"; }
                case "seller" -> { fxmlFile = "SellerView.fxml"; title = "Seller Dashboard"; }
                default       -> { fxmlFile = "UserView.fxml";   title = "Auction"; }
            }

            Platform.runLater(() -> navigateTo(fxmlFile, title, true));

        }).start();
    }

    // Called when Enter is pressed in the username or password field
    @FXML void showUserName(ActionEvent event) { handleLogin(event); }
    @FXML void showPassWord(ActionEvent event) { handleLogin(event); }

    @FXML
    void handleNavigateToRegister(ActionEvent event) {
        navigateTo("Registerview.fxml", "Register", false);
    }

    private void navigateTo(String fxmlFile, String title, boolean resizable) {
        try {
            System.out.println(title);
            String path = "/Client/views/" + fxmlFile;
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setResizable(resizable);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            resetButton();
            showAlert(Alert.AlertType.ERROR, "Lỗi giao diện", "Không thể tải màn hình: " + fxmlFile);
        }
    }

    private void resetButton() {
        btnLogin.setDisable(false);
        btnLogin.setText("Login");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package Client.controller.auth;

import Client.networking.ApiResponse;
import Client.networking.endpoints.UserApi;
import javafx.event.ActionEvent;           // FIX: was java.awt.event.ActionEvent
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.stream.Stream;

public class RegisterController {

    //  User tab fields 
    @FXML private TextField     txtFullName;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;

    //  Seller tab fields 
    @FXML private TextField     txtSellerFullName;
    @FXML private TextField     txtSellerStoreName;
    @FXML private TextField     txtSellerEmail;
    @FXML private PasswordField txtSellerPassword;

    //  Tabs 
    @FXML private Tab tabUser;
    @FXML private Tab tabSeller;

    private final UserApi userApi = new UserApi();

    @FXML
    void handleRegister(ActionEvent event) {
        boolean isSeller = tabSeller != null && tabSeller.isSelected();

        String username  = isSeller ? getText(txtSellerFullName)  : getText(txtFullName);
        String email     = isSeller ? getText(txtSellerEmail)     : getText(txtEmail);
        String password  = isSeller ? getText(txtSellerPassword)  : getText(txtPassword);
        String storeName = isSeller ? getText(txtSellerStoreName) : null;
        String role      = isSeller ? "seller" : "bidder";

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng điền đầy đủ họ tên, email và mật khẩu.");
            return;
        }
        if (isSeller && storeName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng nhập tên cửa hàng.");
            return;
        }

        ApiResponse<Void> response = userApi.register(username, password, email, role, storeName);

        if (response != null && response.getStatus() == 201) {
            String successMsg = isSeller
                    ? "Hồ sơ sẽ được admin duyệt trong vòng 24h."
                    : "Tài khoản đã được tạo. Bạn có thể đăng nhập ngay.";
            showAlert(Alert.AlertType.INFORMATION, "Đăng Ký Thành Công", successMsg);
            clearForm();
        } else {
            String msg = (response != null) ? response.getMessage() : "Mất kết nối tới Server";
            showAlert(Alert.AlertType.ERROR, "Đăng Ký Thất Bại", msg);
        }
    }

    @FXML
    void handleNavigateToLogin(ActionEvent event) {
        try {

            Parent loginView = FXMLLoader.load(getClass().getResource("/Client/views/LoginView.fxml"));
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(new Scene(loginView));
            currentStage.setTitle("Login");
            currentStage.show();
        } catch (IOException e) {
            System.err.println("Không tìm thấy LoginView.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String getText(javafx.scene.control.TextInputControl field) {
        return field != null ? field.getText().trim() : "";
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        Stream.of(txtFullName, txtEmail, txtSellerFullName, txtSellerStoreName, txtSellerEmail)
              .filter(f -> f != null).forEach(TextField::clear);
        Stream.of(txtPassword, txtSellerPassword)
              .filter(f -> f != null).forEach(PasswordField::clear);
    }
}

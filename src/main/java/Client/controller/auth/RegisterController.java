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

public class RegisterController {

    // ── User tab fields (unique fx:ids) ──
    @FXML private TextField     txtFullName;
    @FXML private TextField     txtEmail;
    @FXML private PasswordField txtPassword;      // FIX: PasswordField (was TextField in old FXML)
    @FXML private TextField     txtPhoneNumber;

    // ── Seller tab fields (unique fx:ids — FIX for duplicate-injection crash) ──
    @FXML private TextField     txtSellerFullName;
    @FXML private TextField     txtSellerEmail;
    @FXML private PasswordField txtSellerPassword; // FIX: unique id + PasswordField
    @FXML private TextField     txtCitizenId;
    @FXML private TextField     txtBankAccount;

    // ── Tabs ──
    @FXML private Tab tabUser;
    @FXML private Tab tabSeller;

    private final UserApi userApi = new UserApi();

    @FXML
    void handleRegister(ActionEvent event) {

        if (tabUser != null && tabUser.isSelected()) {
            // ── USER registration ──
            String fullName = txtFullName  != null ? txtFullName.getText().trim()   : "";
            String email    = txtEmail     != null ? txtEmail.getText().trim()       : "";
            String password = txtPassword  != null ? txtPassword.getText().trim()    : "";
            String phone    = txtPhoneNumber != null ? txtPhoneNumber.getText().trim() : "";

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng điền đầy đủ họ tên, email và mật khẩu.");
                return;
            }
            if (phone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Thông báo", "Vui lòng điền số điện thoại.");
                return;
            }

            String username = email.contains("@") ? email.split("@")[0] : email;

            ApiResponse<Void> response = userApi.register(username, password, email, "bidder");
            if (response != null && response.getStatus() == 201) {  // FIX: 201 not 200
                showAlert(Alert.AlertType.INFORMATION, "Đăng Ký Thành Công", "Tài khoản đã được tạo. Bạn có thể đăng nhập ngay.");
                clearForm();
            } else {
                String msg = (response != null) ? response.getMessage() : "Mất kết nối tới Server";
                showAlert(Alert.AlertType.ERROR, "Đăng Ký Thất Bại", msg);
            }

        } else {
            // ── SELLER registration ──
            String fullName  = txtSellerFullName  != null ? txtSellerFullName.getText().trim()  : "";
            String email     = txtSellerEmail     != null ? txtSellerEmail.getText().trim()      : "";
            String password  = txtSellerPassword  != null ? txtSellerPassword.getText().trim()   : "";
            String citizenId = txtCitizenId       != null ? txtCitizenId.getText().trim()        : "";
            String bankAcct  = txtBankAccount     != null ? txtBankAccount.getText().trim()      : "";

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng điền đầy đủ họ tên, email và mật khẩu.");
                return;
            }
            if (citizenId.isEmpty() || bankAcct.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Thông báo", "Vui lòng điền số CCCD và số tài khoản ngân hàng.");
                return;
            }

            String username = email.contains("@") ? email.split("@")[0] : email;

            ApiResponse<Void> response = userApi.register(username, password, email, "seller");
            if (response != null && response.getStatus() == 201) {  // FIX: 201 not 200
                showAlert(Alert.AlertType.INFORMATION, "Đăng Ký Thành Công", "Hồ sơ sẽ được admin duyệt trong vòng 24h.");
                clearForm();
            } else {
                String msg = (response != null) ? response.getMessage() : "Mất kết nối tới Server";
                showAlert(Alert.AlertType.ERROR, "Đăng Ký Thất Bại", msg);
            }
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

    // FIX: single private showAlert — removed conflicting static import from SceneUtil
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        if (txtFullName      != null) txtFullName.clear();
        if (txtEmail         != null) txtEmail.clear();
        if (txtPassword      != null) txtPassword.clear();
        if (txtPhoneNumber   != null) txtPhoneNumber.clear();
        if (txtSellerFullName  != null) txtSellerFullName.clear();
        if (txtSellerEmail     != null) txtSellerEmail.clear();
        if (txtSellerPassword  != null) txtSellerPassword.clear();
        if (txtCitizenId     != null) txtCitizenId.clear();
        if (txtBankAccount   != null) txtBankAccount.clear();
    }
}

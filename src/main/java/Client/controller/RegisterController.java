package Client.controller;

import Client.model.Seller;
import Client.model.User;
import Client.networking.ApiResponse;
import Client.networking.endpoints.UserApi;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static Client.controller.SceneUtil.showAlert;

public class RegisterController {
    @FXML private TextField txtFullName ;
    @FXML private TextField txtEmail ;
    @FXML private PasswordField txtPassword;
    @FXML private TextField txtPhoneNumber ;
    @FXML private TextField txtCitizenId;
    @FXML private TextField txtBankAccount ;
    @FXML private Tab tabUser ;
    @FXML private Tab tabSeller;

    private final UserApi userApi = new UserApi();
    @FXML
    void handleRegister(ActionEvent event) {
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String fullName = txtFullName.getText().trim();
        String username = email.contains("@") ? email.split("@")[0] : email;
        if (email.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi Nhập Liệu", "Vui lòng điền đầy đủ thông tin");
            return;
        }
        //thoi gian tao tk
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        User accountToRegister;
        if (tabUser != null && tabUser.isSelected()) {
            String phone = txtPhoneNumber.getText().trim();
            if (phone.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Thông báo ", "Vui lòng điền số điện thoại");
                return;
            }
            accountToRegister = new User();
            accountToRegister.setRole("USER");
        } else {
            String citizenId = txtCitizenId.getText().trim();
            String bankaccount = txtBankAccount.getText().trim();
            if (citizenId.isEmpty() || bankaccount.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Thông báo điền đầy đủ số CCCD và số tài khoản");
                return;
            }
            Seller sellerUser = new Seller();
            sellerUser.setCitizendId(citizenId);
            sellerUser.setBankAccount(bankaccount);
            accountToRegister = sellerUser;
            accountToRegister.setRole("SELLER");
        }
        accountToRegister.setUsername(username);
        accountToRegister.setPassword(password);
        accountToRegister.setEmail(email);
        accountToRegister.setBalance(0.0);
        accountToRegister.setCreatedAt(currentTime);
        accountToRegister.setUpdatedAt(currentTime);
        ApiResponse<Void> response = userApi.register(accountToRegister);
        if (response != null && response.getStatus() == 200) {
            if (tabSeller != null && tabSeller.isSelected()) {
                ;
                showAlert(Alert.AlertType.INFORMATION, "Đăng Ký Thành Công ", "Hồ sơ sẽ được duyệt trong vòng 24h");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Đăng Ký Thành Công", "Tài khoản đã được tạo");
            }
            clearForm();
        } else {
            String msg = (response != null) ? response.getMessage() : "Mất kết nối tới Server";
            showAlert(Alert.AlertType.ERROR, "Đăng Ký Thất Bại", "Lỗi" + msg);
        }
    }
    @FXML
    // quay lai trang dang nhap
    void handleNavigateToLogin(ActionEvent event) {
        try{
            Parent loginview = FXMLLoader.load(getClass().getResource("/views/LoginView.fxml"));
            Scene loginScene = new Scene(loginview);
            Stage currentStage = (Stage)((Node) event.getSource()).getScene().getWindow();
            currentStage.setScene(loginScene);
            currentStage.show();
        }catch (IOException e ) {
            System.err.println("Không tìm thấy file ,Lỗi : " + e.getMessage());
            e.printStackTrace();
        }
    }
    private void showAlert(Alert.AlertType type , String title , String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void clearForm() {
        txtFullName.clear();
        txtEmail.clear();
        txtPassword().clear();
        if (txtPhoneNumber != null) txtPhoneNumber.clear();
        if (txtCitizenId !null)txtCitizenId.clear();
        if (txtBankAccount != null) txtBankAccount.clear();
    }
}


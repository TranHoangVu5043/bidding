package Client.controller;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class LoginController {

    @FXML private Button btnLogin;
    @FXML private PasswordField txtPassword;  // FIX: txtPassWord → txtPassword (khớp FXML)
    @FXML private TextField txtUserName;

    @FXML
    /*void handleLogin(ActionEvent event) {
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            return;
        }

        try (Socket socket = new Socket("localhost", 1234);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            // FIX: chuẩn hóa protocol, bỏ khoảng trắng thừa quanh '|'
            out.writeUTF("LOGIN|" + user + "|" + pass);
            out.flush();

            String response = in.readUTF().trim();

            if (response.equalsIgnoreCase("SUCCESS_ADMIN")) {
                navigateTo("Adminview.fxml", event);
            } else if (response.equalsIgnoreCase("SUCCESS_SELLER")) {
                navigateTo("SellerView.fxml", event);
            } else if (response.equalsIgnoreCase("SUCCESS_USER")) {
                navigateTo("UserView_.fxml", event);
            } else {
                // FIX: hiển thị Alert thay vì println
                showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tài khoản hoặc mật khẩu không đúng. Vui lòng thử lại.");
            }

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể kết nối đến máy chủ. Vui lòng thử lại sau.");
        }
    }*/
    void handleLogin(ActionEvent event) {
        String user = txtUserName.getText().trim();
        String pass = txtPassword.getText().trim();

        if (user.equals("admin") && pass.equals("123")) {
            navigateTo("Adminview.fxml", event);
        } else if (user.equals("seller") && pass.equals("123")) {
            navigateTo("SellerView.fxml", event);
        } else if (user.equals("user") && pass.equals("123")) {
            navigateTo("UserView.fxml", event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tài khoản hoặc mật khẩu.");
        }
    }

    // FIX: handler thừa trong FXML (onAction="#showUserName" / "#showPassWord") — giữ trống, không cần xử lý gì
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
            e.printStackTrace(); // thêm dòng này
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

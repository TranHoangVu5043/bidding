import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class LoginController {
    @FXML
    private Button btnLogin;
    @FXML
    private PasswordField txtPassWord;
    @FXML
    private TextField txtUserName;

    @FXML
    void handleLogin(ActionEvent event) {
        String user = txtUserName.getText();
        String pass = txtPassWord.getText();
        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Vui lòng nập đầy đủ thông tin");
            return;
        }
        try (Socket socket = new Socket("localhost", 1234);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            out.writeUTF("LOGIN | " + user + "|" + pass);
            out.flush();
            if (in.readUTF().equalsIgnoreCase("SUCCESS")) {
                System.out.println("Đăng nhập thành công");
            } else {
                System.out.println("Tài khoản hoặc mật khẩu không đúng vui lòng thử lại");
            }
        } catch (IOException e) {
            System.out.println("Không kết nối  được ");
        }
    }
}


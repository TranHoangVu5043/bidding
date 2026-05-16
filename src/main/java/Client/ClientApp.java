package Client;

//import Client.networking.ClientConnection;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        File fxml = new File("src/main/java/Client/views/LoginView.fxml");
        Parent root = FXMLLoader.load(fxml.toURI().toURL());
        primaryStage.setTitle("Auction App");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

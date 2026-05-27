package Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class ClientApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        URL fxml = getClass().getResource("/Client/views/LoginView.fxml");
        if (fxml == null) {
            throw new IllegalStateException(
                "Cannot find /Client/views/LoginView.fxml on the classpath. " +
                "Run 'mvn clean javafx:run' to ensure resources are copied."
            );
        }
        Parent root = FXMLLoader.load(fxml);
        primaryStage.setTitle("Auction App");
        primaryStage.setScene(new Scene(root));
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

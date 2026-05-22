package Client.controller.user;

import Client.model.Auction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class AuctionDetailController {
    @FXML
    private Label lblAssetOwner;
    @FXML
    private Label lblResStartTime;
    @FXML
    private Label lblResEndTime;
    @FXML
    private Label lblAuctionStartTime;
    @FXML
    private Label lblAuctionEndTime;
    @FXML
    private Label lblStartingPrice;
    @FXML
    private Label lblPriceStep;
    @FXML
    private Label lblDepositAmount;
    @FXML
    private Label lblParticipationFee;

    private Auction currentAuction;

    // nhan  du lieu tu  User
    public void setAuctionData(Auction auction) {
        this.currentAuction = auction;
        if (auction != null) {
            lblAssetOwner.setText(auction.getOwnerId()+ "");
            lblResStartTime.setText("17:00 28/04/2026"); // mẫu
            lblResEndTime.setText("17:00 19/05/2026");
            lblAuctionStartTime.setText(auction.getStartTime());
            lblAuctionEndTime.setText(auction.getEndTime());
            double startPrice = auction.getStartingPrice();
            lblStartingPrice.setText(formatVND(startPrice));
            lblPriceStep.setText(formatVND(10000000.0));
            lblDepositAmount.setText(formatVND(startPrice * 0.1));
            lblParticipationFee.setText(formatVND(600000.0));
        }
    }

    // nhan nut dang ki
    @FXML
    private void handleRegisterAuction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng ký thành công");
        alert.setHeaderText(null);
        alert.setContentText("Đăng kí phiên đấu giá #" + currentAuction.getId() + "thành công");
        alert.showAndWait();

        returnToUserView(event);
    }

    private void returnToUserView(ActionEvent event) {
        try {
            File fxml = new File("src/main/java/Client/views/UserView.fxml");
            Parent root = FXMLLoader.load(fxml.toURI().toURL());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatVND(double amount) {
        return String.format("%,.0f VNĐ", amount);
    }
}


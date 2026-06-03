package Client.controller.user.helpers;

import Client.model.auction.Auction;
import Client.model.auction.BidHistoryItem;
import Client.util.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public final class BidHistoryRenderer {

    private BidHistoryRenderer() {}

    public static HBox buildCard(BidHistoryItem item) {
        Auction a   = item.getAuction();
        boolean won = item.isWon();

        //Outcome badge
        String outcomeColor = won ? "#16A34A" : "#D32F2F";
        String outcomeText  = won ? "🏆 Đã thắng" : "❌ Đã thua";
        Label lblOutcome = new Label(outcomeText);
        lblOutcome.setStyle("-fx-text-fill: white; -fx-font-size: 11; -fx-font-weight: bold; " +
                "-fx-background-color: " + outcomeColor + "; " +
                "-fx-background-radius: 6; -fx-padding: 3 8;");

        //Info column
        Label lblTitle = new Label("Phiên đấu giá #" + (a != null ? a.getId() : "?"));
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #1e293b;");

        String itemStr = (a != null && a.getItemName() != null && !a.getItemName().isBlank())
                ? a.getItemName() : (a != null ? "Mặt hàng #" + a.getItemId() : "—");
        Label lblItem = new Label("📦 " + itemStr);
        lblItem.setStyle("-fx-font-size: 12; -fx-text-fill: #374151; -fx-font-weight: bold;");

        String sellerStr = (a != null && a.getSellerName() != null) ? a.getSellerName() : "—";
        Label lblSeller = new Label("🏪 " + sellerStr);
        lblSeller.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        Label lblEndDate = new Label("📅 Kết thúc: " +
                (a != null ? DialogUtil.formatDisplayTime(a.getEndTime()) : "—"));
        lblEndDate.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        Label lblMyBid = new Label(String.format("💰 Giá đặt cao nhất của bạn: %,.0f ₫", item.getMyHighestBid()));
        lblMyBid.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: " + outcomeColor + ";");

        double winPrice = a != null ? a.getCurrentPrice() : 0;
        Label lblWinPrice = new Label(String.format("🔔 Giá thắng cuộc: %,.0f ₫", winPrice));
        lblWinPrice.setStyle("-fx-font-size: 11; -fx-text-fill: #374151;");

        Label lblCount = new Label("🔢 Số lần đặt giá: " + item.getMyBidCount());
        lblCount.setStyle("-fx-font-size: 11; -fx-text-fill: #6B7280;");

        VBox infoBox = new VBox(5, lblTitle, lblItem, lblSeller, lblEndDate, lblMyBid, lblWinPrice, lblCount);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        //Left accent bar
        Region bar = new Region();
        bar.setPrefWidth(5);
        bar.setMinHeight(80);
        bar.setStyle("-fx-background-color: " + outcomeColor + "; -fx-background-radius: 4 0 0 4;");

        //Badge column
        VBox badgeCol = new VBox(lblOutcome);
        badgeCol.setAlignment(Pos.CENTER);
        badgeCol.setPrefWidth(90);

        HBox card = new HBox(0, bar, infoBox, badgeCol);
        card.setPadding(new Insets(14, 14, 14, 10));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setSpacing(12);
        String bg     = won ? "#F0FDF4" : "#FFF5F5";
        String border = won ? "#BBF7D0" : "#FECACA";
        card.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 10; " +
                "-fx-border-color: " + border + "; -fx-border-radius: 10; -fx-border-width: 1;");
        return card;
    }
}

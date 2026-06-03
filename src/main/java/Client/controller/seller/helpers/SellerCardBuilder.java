package Client.controller.seller.helpers;

import Client.model.auction.Auction;
import Client.model.item.Item;
import Client.util.DialogUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.function.Consumer;

public final class SellerCardBuilder {

    private SellerCardBuilder() {}

    // ── Auction card ──────────────────────────────────────────────────────

    /**
     * @param onClick  called with the auction when the user clicks the card
     */
    public static VBox buildAuctionCard(Auction auction, List<Item> masterData,
                                        Consumer<Auction> onClick) {

        Item item = masterData.stream()
                .filter(i -> i.getId() == auction.getItemId())
                .findFirst().orElse(null);

        String itemName = item != null ? item.getName() : "SP #" + auction.getItemId();
        String category = item != null ? item.getCategory() : "";

        Label iconLabel = new Label(DialogUtil.categoryEmoji(category));
        iconLabel.setStyle("-fx-font-size: 46;");

        javafx.scene.layout.StackPane imageArea = new javafx.scene.layout.StackPane(iconLabel);
        imageArea.setPrefSize(170, 125);
        imageArea.setStyle("-fx-background-color: " + DialogUtil.categoryGradient(category) +
                "; -fx-background-radius: 10 10 0 0;");

        Label priceLabel = new Label(String.format("$ %,.0f", auction.getCurrentPrice()));
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 13; -fx-padding: 5 10;");

        Label nameLabel = new Label(itemName);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(154);
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label statusLabel = new Label(auction.getStatus());
        statusLabel.setStyle("-fx-background-color: " + DialogUtil.statusColor(auction.getStatus()) +
                "; -fx-text-fill: white; -fx-font-size: 10; -fx-font-weight: bold;" +
                "-fx-padding: 2 7; -fx-background-radius: 4;");

        HBox statusBox = new HBox(statusLabel);
        statusBox.setPadding(new Insets(4, 0, 0, 0));

        VBox info = new VBox(4, nameLabel, statusBox);
        info.setPadding(new Insets(8, 8, 10, 8));

        VBox card = new VBox(imageArea, priceLabel, info);
        card.setPrefWidth(170);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 8, 0, 0, 3);" +
                "-fx-cursor: hand;");
        if (onClick != null) card.setOnMouseClicked(e -> onClick.accept(auction));
        return card;
    }

    // ── Item (inventory) card ─────────────────────────────────────────────

    /** sellerAuctions is used to show the auction-status overlay on the card. */
    public static VBox buildItemCard(Item item, List<Auction> sellerAuctions) {
        Label iconLabel = new Label(DialogUtil.categoryEmoji(item.getCategory()));
        iconLabel.setStyle("-fx-font-size: 38;");

        javafx.scene.layout.StackPane imageArea = new javafx.scene.layout.StackPane(iconLabel);
        imageArea.setPrefSize(160, 110);
        imageArea.setStyle("-fx-background-color: " + DialogUtil.categoryGradient(item.getCategory()) +
                "; -fx-background-radius: 10 10 0 0;");

        Label priceLabel = new Label(String.format("$ %,.0f", item.getPrice()));
        priceLabel.setMaxWidth(Double.MAX_VALUE);
        priceLabel.setAlignment(Pos.CENTER);
        priceLabel.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 12; -fx-padding: 4 8;");

        Label nameLabel = new Label(item.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(144);
        nameLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        String catColor = DialogUtil.categoryColor(item.getCategory());
        Label catLabel = new Label(item.getCategory() != null ? item.getCategory() : "OTHER");
        catLabel.setStyle("-fx-background-color: " + catColor + "; -fx-text-fill: white;" +
                "-fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 6; -fx-background-radius: 4;");

        long activeCount = sellerAuctions.stream()
                .filter(a -> a.getItemId() == item.getId()
                        && ("ACTIVE".equalsIgnoreCase(a.getStatus())
                        ||  "UPCOMING".equalsIgnoreCase(a.getStatus())))
                .count();
        boolean isOnAuction = activeCount > 0;
        boolean isActive    = sellerAuctions.stream()
                .anyMatch(a -> a.getItemId() == item.getId()
                        && "ACTIVE".equalsIgnoreCase(a.getStatus()));

        Label stockLabel = new Label("Kho: " + item.getStock()
                + (isOnAuction ? "  •  " + activeCount + " đấu giá" : ""));
        stockLabel.setStyle("-fx-font-size: 9; -fx-text-fill: #9ca3af;");

        HBox catBox = new HBox(6, catLabel);
        catBox.setPadding(new Insets(4, 0, 0, 0));
        VBox info = new VBox(3, nameLabel, catBox, stockLabel);
        info.setPadding(new Insets(7, 8, 10, 8));

        if (isOnAuction) {
            String overlayColor = isActive ? "#22c55e" : "#3b82f6";
            String overlayText  = isActive ? "● Đang diễn ra" : "● Sắp diễn ra";
            Label overlay = new Label(overlayText);
            overlay.setStyle("-fx-background-color: " + overlayColor + "; -fx-text-fill: white;" +
                    "-fx-font-size: 9; -fx-font-weight: bold; -fx-padding: 2 7; -fx-background-radius: 0 0 6 0;");
            javafx.scene.layout.StackPane.setAlignment(overlay, javafx.geometry.Pos.TOP_LEFT);
            imageArea.getChildren().add(overlay);
        }

        VBox card = new VBox(imageArea, priceLabel, info);
        card.setPrefWidth(160);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;" +
                "-fx-effect: dropshadow(gaussian,rgba(0,0,0,0.10),8,0,0,3); -fx-cursor: hand;");
        return card;
    }
}

package Server.model.auction;

import Server.model.auction.items.Art;
import Server.model.auction.items.Electronics;
import Server.model.auction.items.Item;
import Server.model.auction.items.Vehicle;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemFactory {

    public static Item createItem(String category, int id, String name, String desc,
                                  int ownerId, String condition, ResultSet rs) throws SQLException {
        double price = rs.getDouble("price");
        int stock    = rs.getInt("stock");
        if (category == null) category = "UNKNOWN";
        return switch (category.toUpperCase()) {
            case "ELECTRONICS" -> new Electronics(id, name, desc, ownerId, category, condition, price, stock);
            case "ART"         -> new Art(id, name, desc, ownerId, category, condition, price, stock);
            case "VEHICLE"     -> new Vehicle(id, name, desc, ownerId, category, condition, price, stock);
            default            -> new Item(id, name, desc, ownerId, category, condition, price, stock) {
                @Override public void displayInfo() {
                    System.out.println("[Item] " + getName() + " | Category: " + getCategory());
                }
            };
        };
    }
}

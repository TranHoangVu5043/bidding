package Server.model.auction;

import Server.model.auction.items.Art;
import Server.model.auction.items.Electronics;
import Server.model.auction.items.Item;
import Server.model.auction.items.Vehicle;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemFactory {
    public static Item createItem(String category, int id, String name, String desc, int ownerId, String condition, ResultSet rs) throws SQLException {
        return switch (category.toUpperCase()) {
            case "ELECTRONICS" -> new Electronics(
                    id, name, desc, ownerId, category, condition,
                    rs.getString("warranty_period"),
                    rs.getDouble("weight")
            );
            case "ART" -> new Art(
                    id, name, desc, ownerId, category, condition,
                    rs.getString("artist"),
                    rs.getString("material"),
                    rs.getString("certificate")
            );
            case "VEHICLE" -> new Vehicle(
                    id, name, desc, ownerId, category, condition,
                    rs.getString("manuFacturer"),
                    rs.getString("fuelType"),
                    rs.getString("license_plate")
            );
            default -> throw new IllegalArgumentException("Unknown category: " + category);
        };
    }
}
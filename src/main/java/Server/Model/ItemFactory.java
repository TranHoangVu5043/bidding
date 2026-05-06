package Server.Model;

import java.time.LocalDateTime;

public class ItemFactory {
    //    public static Item createItem(String type, String id, String name, String desc, double price, LocalDateTime end, String extraInfo, ) {
//        switch (type.toUpperCase()) {
//            case "ELECTRONICS":
//                return new Electronics(id, name, desc, price, end, extraInfo);
//            case "ART":
//                return new Art(id, name, desc, price, end, extraInfo);
//            case "VEHICLE":
//                // Giả sử extraInfo của Vehicle là loại động cơ
//                return new Vehicle(id, name, desc, price, end, extraInfo);
//            default:
//                throw new IllegalArgumentException("Loại sản phẩm không hợp lệ!");
//        }
    // }
    public static Electronics createElectronics(String id, String name, String desc, double price,
                                                LocalDateTime end, String warranty, double weight) {
        return new Electronics(id, name, desc, price, end, warranty, weight);
    }

    public static Art createArt(String id, String name, String desc, double price,
                                LocalDateTime end, String artist, String material, String cert) {
        return new Art(id, name, desc, price, end, artist, material, cert);
    }
    public static Vehicle createVehicle(String id, String name, String desc, double price,  LocalDateTime end,
                                        String manuFacturer, String fuelType, String licensePlate){
        return new Vehicle(id, name, desc, price, end, manuFacturer, fuelType, licensePlate);
    }
}
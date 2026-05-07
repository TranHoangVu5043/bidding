package Server.Model;

import java.time.LocalDateTime;

public class ItemFactory {
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
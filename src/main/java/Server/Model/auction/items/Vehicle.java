package Server.model.auction.items;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String manuFacturer; // hãng xe;
    private String fuelType; //loại nhiên liệu sử dụng
    private String licensePlate; //biển số xe
    public Vehicle(String id, String name, String description, double startingPrice, LocalDateTime
            endTime, String manuFacturer, String fuelType, String licensePlate){
        super(id, name, description, startingPrice, endTime);
        this.manuFacturer = manuFacturer;
        this.fuelType = fuelType;
        this.licensePlate = licensePlate;
    }
}

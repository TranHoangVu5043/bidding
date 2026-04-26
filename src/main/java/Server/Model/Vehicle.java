package Server.Model;

import java.time.LocalDateTime;

public class Vehicle extends Item {
    private String manuFacturer; // hãng xe;
    private String fuelType; //loại nhiên liệu sử dụng
    private String modelYear;
    private double weight; //cân nặng;
    private int capacity; //số chỗ ngồi
    private String licensePlate; //biển số xe
    public Vehicle(String id, String name, String description, double startingPrice, double currentPrice, LocalDateTime
            startTime, LocalDateTime endTime, String sellerId, String idHighestBidder, String status, String manuFacturer, String fuelType, String modelYear, double weight, int capacity, String licensePlate){
        super(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, idHighestBidder, status);
        this.manuFacturer = manuFacturer;
        this.fuelType = fuelType;
        this.modelYear = modelYear;
        this.weight = weight;
        this.capacity = capacity;
        this.licensePlate = licensePlate;
    }
}

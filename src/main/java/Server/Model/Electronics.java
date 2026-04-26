package Server.Model;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String warrantyPeriod;
    private double weight; //cân nặng;
    public Electronics(String id, String name, String description, double startingPrice, double currentPrice, LocalDateTime
            startTime, LocalDateTime endTime, String sellerId, String idHighestBidder, String status, String warrantyPeriod, double weight){
        super(id, name, description, startingPrice, currentPrice, startTime, endTime, sellerId, idHighestBidder, status);
        this.warrantyPeriod = warrantyPeriod;
        this.weight = weight;
    }
}

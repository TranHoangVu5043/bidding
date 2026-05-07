package Server.model.auction.items;

import java.time.LocalDateTime;

public class Electronics extends Item {
    private String warrantyPeriod;
    private double weight; //cân nặng;
    public Electronics(String id, String name, String description, double startingPrice,LocalDateTime
            endTime, String warrantyPeriod, double weight){
        super(id, name, description, startingPrice, endTime);
        this.warrantyPeriod = warrantyPeriod;
        this.weight = weight;
    }
}

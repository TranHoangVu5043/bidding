package Server.model.auction;

import java.time.LocalDateTime;

public class Auction {

    private int id;
    private int itemId;
    private int ownerId;

    private double startingPrice;
    private double currentPrice;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;

    public Auction(int id,
                   int itemId,
                   int ownerId,
                   double startingPrice,
                   double currentPrice,
                   LocalDateTime startTime,
                   LocalDateTime endTime,
                   String status) {

        this.id = id;
        this.itemId = itemId;
        this.ownerId = ownerId;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public int getItemId() {
        return itemId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean hasEnded() {
        return endTime.isBefore(LocalDateTime.now());
    }
}
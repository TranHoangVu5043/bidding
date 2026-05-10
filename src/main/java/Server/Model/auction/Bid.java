package Server.model.auction;

import java.time.LocalDateTime;

public class Bid {

    private int id;
    private int userId;
    private int auctionId;
    private double amount;
    private LocalDateTime createdAt;

    public Bid(int id,
               int userId,
               int auctionId,
               double amount,
               LocalDateTime createdAt) {

        this.id = id;
        this.userId = userId;
        this.auctionId = auctionId;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
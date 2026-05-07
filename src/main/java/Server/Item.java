package Server;

import java.time.LocalDateTime;

public class Item {
    private String id;
    private String name;
    private String description;
    private double startingPrice;
    private double currentPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String sellerId;
    private String idHighestBidder;
    private String status;

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getStartingPrice() {
        return startingPrice;
    }

    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getIdHighestBidder() {
        return idHighestBidder;
    }

    public void setIdHighestBidder(String idHighestBidder) {
        this.idHighestBidder = idHighestBidder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Item(String id, String name, String description, double startingPrice, double currentPrice, LocalDateTime
            startTime, LocalDateTime endTime, String sellerId, String idHighestBidder, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.startTime = startTime;
        this.endTime = endTime;
        this.idHighestBidder = idHighestBidder;
        this.status = status;
        this.sellerId = sellerId;
    }

    public synchronized boolean placeBid(double amount, String bidderId) {
        if (amount <= currentPrice) {
            System.out.println("The bid amount must be greater than the current price.");
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)){
            System.out.println("The auction has not started yet.");
            return false;
        }
        if (now.isAfter(endTime)) {
            System.out.println("The auction has ended.");
            this.status = "FINISHED";
            return false;
        }
        this.currentPrice = amount;
        this.idHighestBidder = bidderId;
        return true;
    }
}

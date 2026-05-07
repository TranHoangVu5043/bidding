package Server.model.auction;

import Server.model.auction.items.Item;

import java.time.LocalDateTime;

public class Auction {
    private String auctionId;
    private Item item;
    private AuctionStatus status;
    private double currentPrice;
    private String leadingBidderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Auction(String auctionId, Item item, LocalDateTime startTime, LocalDateTime endTime) {
        this.auctionId = auctionId;
        this.item = item;
        this.status = AuctionStatus.OPEN;
        this.currentPrice = item.getStartingPrice();
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public void updateStatus() {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) status = AuctionStatus.OPEN;
        else if (now.isAfter(startTime) && now.isBefore(endTime)) status = AuctionStatus.RUNNING;
        else if (now.isAfter(endTime) && status == AuctionStatus.RUNNING) status = AuctionStatus.FINISHED;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public Item getItem() {
        return item;
    }
}


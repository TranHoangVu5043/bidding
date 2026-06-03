package Server.dto.responses;

import Server.model.auction.Bid;

public class BidDTO {
    public int    id, userId, auctionId;
    public double amount;
    public String createdAt;
    public String username;

    public BidDTO(Bid b, String username) {
        id        = b.getId();
        userId    = b.getUserId();
        auctionId = b.getAuctionId();
        amount    = b.getAmount();
        createdAt = b.getCreatedAt() != null ? b.getCreatedAt().toString() : null;
        this.username = username;
    }
}

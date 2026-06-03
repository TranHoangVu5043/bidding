package Client.dto.requests;

public class AutoBidBody {
    public final int    auctionId;
    public final double maxBid;
    public final double increment;

    public AutoBidBody(int auctionId, double maxBid, double increment) {
        this.auctionId = auctionId;
        this.maxBid    = maxBid;
        this.increment = increment;
    }
}

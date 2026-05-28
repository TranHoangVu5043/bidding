package Client.dto.requests;

public class PlaceBidBody {
    public final int    auctionId;
    public final double amount;

    public PlaceBidBody(int auctionId, double amount) {
        this.auctionId = auctionId;
        this.amount    = amount;
    }
}

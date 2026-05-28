package Client.dto.requests;

public class CreateAuctionBody {
    public final int    itemId;
    public final double startingPrice;
    public final String startTime;
    public final String endTime;

    public CreateAuctionBody(int itemId, double startingPrice, String startTime, String endTime) {
        this.itemId        = itemId;
        this.startingPrice = startingPrice;
        this.startTime     = startTime;
        this.endTime       = endTime;
    }
}

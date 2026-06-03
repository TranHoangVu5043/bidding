package Server.dto.responses;

import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

public class BidHistoryDTO {
    public AuctionDTO auction;
    public double     myHighestBid;
    public int        myBidCount;
    public boolean    won;

    public BidHistoryDTO(BiddingService.BidHistoryEntry e, UserService userService, ItemService itemService) {
        this.auction      = new AuctionDTO(e.auction(), userService, itemService);
        this.myHighestBid = e.myHighestBid();
        this.myBidCount   = e.myBidCount();
        this.won          = e.won();
    }
}

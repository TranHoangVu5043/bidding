package Server.dto.responses;

import Server.model.auction.items.Item;
import Server.model.users.User;
import Server.service.auction.BiddingService;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

public class BidHistoryDTO {
    public AuctionDTO auction;
    public double     myHighestBid;
    public int        myBidCount;
    public boolean    won;

    public BidHistoryDTO(BiddingService.BidHistoryEntry e, UserService userService, ItemService itemService) {
        this(e,
             userService != null ? userService.getUserById(e.auction().getOwnerId()) : null,
             itemService != null ? itemService.getItemDetail(e.auction().getItemId()) : null);
    }

    /** Batch-friendly constructor — caller pre-fetches seller and item to avoid N+1 queries. */
    public BidHistoryDTO(BiddingService.BidHistoryEntry e, User seller, Item item) {
        this.auction      = new AuctionDTO(e.auction(), seller, item);
        this.myHighestBid = e.myHighestBid();
        this.myBidCount   = e.myBidCount();
        this.won          = e.won();
    }
}

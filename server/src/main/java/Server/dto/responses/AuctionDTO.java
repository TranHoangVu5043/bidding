package Server.dto.responses;

import Server.model.auction.Auction;
import Server.model.auction.items.Item;
import Server.model.users.User;
import Server.service.auction.ItemService;
import Server.service.users.UserService;

public class AuctionDTO {
    public int id, itemId, ownerId;
    public double startingPrice, currentPrice;
    public String startTime, endTime, status;
    public String sellerName;
    public String itemName;
    public String highestBidderName;

    /** Backwards-compatible constructor — itemName will be null. */
    public AuctionDTO(Auction a, UserService userService) {
        this(a, userService, null);
    }

    /** Full constructor that also resolves the item name. */
    public AuctionDTO(Auction a, UserService userService, ItemService itemService) {
        this(a,
             userService != null ? userService.getUserById(a.getOwnerId()) : null,
             itemService != null ? itemService.getItemDetail(a.getItemId()) : null);
    }

    /** Batch-friendly constructor — caller pre-fetches seller, item and highest bidder to avoid N+1 queries. */
    public AuctionDTO(Auction a, User seller, Item item, User highestBidder) {
        id                 = a.getId();
        itemId             = a.getItemId();
        ownerId            = a.getOwnerId();
        startingPrice      = a.getStartingPrice();
        currentPrice       = a.getCurrentPrice();
        startTime          = a.getStartTime() != null ? a.getStartTime().toString() : null;
        endTime            = a.getEndTime()   != null ? a.getEndTime().toString()   : null;
        status             = a.getStatus();
        sellerName         = seller != null ? seller.getUsername() : "Seller #" + a.getOwnerId();
        itemName           = item   != null ? item.getName()       : null;
        highestBidderName  = highestBidder != null ? highestBidder.getUsername() : null;
    }

    /** Backwards-compatible 3-arg constructor — no highest bidder. */
    public AuctionDTO(Auction a, User seller, Item item) {
        this(a, seller, item, null);
    }
}

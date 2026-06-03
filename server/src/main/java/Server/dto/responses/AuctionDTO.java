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

    /** Backwards-compatible constructor — itemName will be null. */
    public AuctionDTO(Auction a, UserService userService) {
        this(a, userService, null);
    }

    /** Full constructor that also resolves the item name. */
    public AuctionDTO(Auction a, UserService userService, ItemService itemService) {
        id            = a.getId();
        itemId        = a.getItemId();
        ownerId       = a.getOwnerId();
        startingPrice = a.getStartingPrice();
        currentPrice  = a.getCurrentPrice();
        startTime     = a.getStartTime() != null ? a.getStartTime().toString() : null;
        endTime       = a.getEndTime()   != null ? a.getEndTime().toString()   : null;
        status        = a.getStatus();
        User seller   = userService.getUserById(a.getOwnerId());
        sellerName    = seller != null ? seller.getUsername() : "Seller #" + a.getOwnerId();
        if (itemService != null) {
            Item item = itemService.getItemDetail(a.getItemId());
            itemName  = item != null ? item.getName() : null;
        }
    }
}

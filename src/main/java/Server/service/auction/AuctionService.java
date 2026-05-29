package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.dao.users.UserDAO;
import Server.model.auction.Auction;
import Server.model.auction.Bid;
import Server.model.auction.items.Item;
import Server.service.NotificationService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final ItemDAO itemDAO;
    private final UserDAO userDAO;
    private final NotificationService notifService;

    public AuctionService(AuctionDAO auctionDAO, BidDAO bidDAO, ItemDAO itemDAO) {
        this(auctionDAO, bidDAO, itemDAO, null, null);
    }

    public AuctionService(AuctionDAO auctionDAO, BidDAO bidDAO, ItemDAO itemDAO,
                          UserDAO userDAO, NotificationService notifService) {
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.itemDAO = itemDAO;
        this.userDAO = userDAO;
        this.notifService = notifService;
    }

    public Auction createAuction(Auction auction, int userId) {
        Item item = itemDAO.findById(auction.getItemId());
        if (item == null) return null;
        if (item.getOwnerId() != userId) return null;

        LocalDateTime now = LocalDateTime.now();
        auction.setStatus(auction.getStartTime().isAfter(now) ? "UPCOMING" : "ACTIVE");
        auction.setCurrentPrice(auction.getStartingPrice());
        return auctionDAO.create(auction);
    }

    public Auction getAuction(int auctionId) {
        return auctionDAO.findById(auctionId);
    }

    public List<Auction> getAllAuctions() {
        return auctionDAO.findAll();
    }

    public List<Auction> getAuctionsByOwner(int ownerId) {
        return auctionDAO.findByOwnerId(ownerId);
    }

    /**
     * Cancels an auction. Only allowed while the auction has not yet finished or been finalized.
     */
    public boolean cancelAuction(int auctionId, int userId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return false;
        if (auction.getOwnerId() != userId) return false;

        String status = auction.getStatus();
        if ("FINISHED".equals(status) || "PAID".equals(status)) {
            return false; // cannot cancel a concluded auction
        }

        // 1. Refund bidders BEFORE deleting rows (bids must still be readable)
        //    Each user paid their highest bid under the delta-charge system.
        List<Bid> bids = bidDAO.getBidsByAuction(auctionId);
        Map<Integer, Double> refunds = new HashMap<>();
        for (Bid b : bids) {
            refunds.merge(b.getUserId(), b.getAmount(), Math::max);
        }
        for (Map.Entry<Integer, Double> entry : refunds.entrySet()) {
            int bidderId = entry.getKey();
            double amount = entry.getValue();
            if (userDAO != null) {
                userDAO.addBalance(bidderId, amount);
            }
            if (notifService != null) {
                notifService.send(bidderId,
                    String.format("Phiên đấu giá #%d đã bị hủy. %,.0f ₫ đã được hoàn lại vào tài khoản của bạn.",
                        auctionId, amount));
            }
        }

        // 2. Delete bids first (avoids FK constraint if no CASCADE), then the auction itself
        bidDAO.deleteByAuctionId(auctionId);
        auctionDAO.deleteAuction(auctionId);

        return true;
    }

    /**
     * Syncs auction status with the current time.
     * UPCOMING → ACTIVE → FINISHED (terminal states PAID/CANCELED are never overwritten).
     */
    public void refreshAuctionStatus(int auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return;

        String current = auction.getStatus();
        if ("PAID".equals(current)) return; // terminal state — cancelled auctions are deleted

        LocalDateTime now = LocalDateTime.now();

        String newStatus;
        if (now.isBefore(auction.getStartTime())) {
            newStatus = "UPCOMING";
        } else if (now.isBefore(auction.getEndTime())) {
            newStatus = "ACTIVE";
        } else {
            newStatus = "FINISHED";
        }

        if (!current.equals(newStatus)) {
            // updateStatus returns true only on the first actual row change, preventing double-refunds
            boolean didTransition = auctionDAO.updateStatus(auctionId, newStatus);

            if (didTransition && "FINISHED".equals(newStatus)) {
                Integer winnerId = bidDAO.findHighestBidder(auctionId);

                // Refund every losing bidder their highest bid (they only ever paid that amount)
                if (userDAO != null) {
                    List<Bid> bids = bidDAO.getBidsByAuction(auctionId);
                    Map<Integer, Double> maxBidPerUser = new HashMap<>();
                    for (Bid b : bids) {
                        maxBidPerUser.merge(b.getUserId(), b.getAmount(), Math::max);
                    }
                    for (Map.Entry<Integer, Double> entry : maxBidPerUser.entrySet()) {
                        int bidderId = entry.getKey();
                        if (winnerId != null && bidderId == winnerId) continue; // winner keeps their payment
                        double refund = entry.getValue();
                        userDAO.addBalance(bidderId, refund);
                        if (notifService != null) {
                            notifService.send(bidderId,
                                String.format("Phiên đấu giá #%d đã kết thúc. %,.0f ₫ đã được hoàn lại vào tài khoản của bạn.",
                                    auctionId, refund));
                        }
                    }
                }

                // Notify winner
                if (winnerId != null && notifService != null) {
                    notifService.send(winnerId,
                        String.format("🎉 Chúc mừng! Bạn đã thắng phiên đấu giá #%d với giá %,.0f ₫!",
                            auctionId, auction.getCurrentPrice()));
                }
            }
        }
    }
//    public List<Auction> getAllActiveAuctions(){
//        List<Auction> list = auctionDAO.getActiveAuctions();
//        return list;
//    }

    /**
     * Marks the auction as FINISHED and returns the winner's user ID, or null if no bids.
     */
    public Integer finalizeAuction(int auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return null;

        auctionDAO.updateStatus(auctionId, "FINISHED");

        List<Bid> bids = bidDAO.getBidsByAuction(auctionId);
        if (bids.isEmpty()) return null;

        return bids.stream()
                .max((a, b) -> Double.compare(a.getAmount(), b.getAmount()))
                .map(Bid::getUserId)
                .orElse(null);
    }
}

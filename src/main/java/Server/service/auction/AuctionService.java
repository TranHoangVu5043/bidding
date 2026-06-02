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
import java.time.ZoneOffset;
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

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
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

    // shortcut — non-admin cancels default to ownership check
    public boolean cancelAuction(int auctionId, int userId) {
        return cancelAuction(auctionId, userId, false);
    }

    public boolean cancelAuction(int auctionId, int userId, boolean byAdmin) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return false;
        if (!byAdmin && auction.getOwnerId() != userId) return false;

        String status = auction.getStatus();
        if ("FINISHED".equals(status) || "PAID".equals(status)) {
            return false; // can't cancel something that's already done
        }

        // refund everyone BEFORE deleting anything — we still need to read the bid rows
        // each user only ever paid their single highest bid, so that's what we give back
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

        // bids first to avoid FK violations, then the auction row itself
        bidDAO.deleteByAuctionId(auctionId);
        auctionDAO.deleteAuction(auctionId);

        return true;
    }

    // looks at the current time and moves the auction to the right status
    // UPCOMING → ACTIVE → FINISHED; PAID is terminal and won't be touched
    public void refreshAuctionStatus(int auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return;

        String current = auction.getStatus();
        if ("PAID".equals(current)) return; // terminal — leave it alone

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        String newStatus;
        if (now.isBefore(auction.getStartTime())) {
            newStatus = "UPCOMING";
        } else if (now.isBefore(auction.getEndTime())) {
            newStatus = "ACTIVE";
        } else {
            newStatus = "FINISHED";
        }

        if (!current.equals(newStatus)) {
            // updateStatus only returns true on the first actual DB change,
            // so refund logic can't double-fire even if this runs twice quickly
            boolean didTransition = auctionDAO.updateStatus(auctionId, newStatus);

            if (didTransition && "FINISHED".equals(newStatus)) {
                Integer winnerId = bidDAO.findHighestBidder(auctionId);

                // losers get their money back; the winner already paid so they keep it
                if (userDAO != null) {
                    List<Bid> bids = bidDAO.getBidsByAuction(auctionId);
                    Map<Integer, Double> maxBidPerUser = new HashMap<>();
                    for (Bid b : bids) {
                        maxBidPerUser.merge(b.getUserId(), b.getAmount(), Math::max);
                    }
                    for (Map.Entry<Integer, Double> entry : maxBidPerUser.entrySet()) {
                        int bidderId = entry.getKey();
                        if (winnerId != null && bidderId == winnerId) continue;
                        double refund = entry.getValue();
                        userDAO.addBalance(bidderId, refund);
                        if (notifService != null) {
                            notifService.send(bidderId,
                                String.format("Phiên đấu giá #%d đã kết thúc. %,.0f ₫ đã được hoàn lại vào tài khoản của bạn.",
                                    auctionId, refund));
                        }
                    }
                }

                // let the winner know they won
                if (winnerId != null && notifService != null) {
                    notifService.send(winnerId,
                        String.format("🎉 Chúc mừng! Bạn đã thắng phiên đấu giá #%d với giá %,.0f ₫!",
                            auctionId, auction.getCurrentPrice()));
                }
            }
        }
    }

    // force-finish and return who won (null = no bids)
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

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

    public boolean cancelAuction(int auctionId, int userId) {
        return cancelAuction(auctionId, userId, false);
    }

    public boolean cancelAuction(int auctionId, int userId, boolean byAdmin) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return false;
        if (!byAdmin && auction.getOwnerId() != userId) return false;

        String status = auction.getStatus();
        if ("FINISHED".equals(status) || "PAID".equals(status)) {
            return false;
        }

        // refund before deletion
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

        // bids first
        bidDAO.deleteByAuctionId(auctionId);
        auctionDAO.deleteAuction(auctionId);

        return true;
    }

    public void refreshAuctionStatus(int auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return;

        String current = auction.getStatus();
        if ("PAID".equals(current)) return;

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
            boolean didTransition = auctionDAO.updateStatus(auctionId, newStatus);

            if (didTransition && "FINISHED".equals(newStatus)) {
                Integer winnerId = bidDAO.findHighestBidder(auctionId);

                if (userDAO != null) {
                    List<Bid> bids = bidDAO.getBidsByAuction(auctionId);
                    Map<Integer, Double> maxBidPerUser = new HashMap<>();

                    for (Bid b : bids) {
                        maxBidPerUser.merge(b.getUserId(), b.getAmount(), Math::max);
                    }

                    // refund
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

                // Noti
                if (winnerId != null && notifService != null) {
                    notifService.send(winnerId,
                        String.format("🎉 Chúc mừng! Bạn đã thắng phiên đấu giá #%d với giá %,.0f ₫!",
                            auctionId, auction.getCurrentPrice()));
                }
            }
        }
    }

    /** Seller-initiated early finish — only works on ACTIVE auctions owned by the caller. */
    public boolean finishEarly(int auctionId, int ownerId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return false;
        if (auction.getOwnerId() != ownerId) return false;
        if (!"ACTIVE".equals(auction.getStatus())) return false;
        finalizeAuction(auctionId);
        return true;
    }

    // force-finish
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

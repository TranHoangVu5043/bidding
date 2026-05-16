package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.model.auction.Auction;
import Server.model.auction.Bid;
import Server.model.auction.items.Item;

import java.time.LocalDateTime;
import java.util.List;

public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final ItemDAO itemDAO;

    public AuctionService(AuctionDAO auctionDAO, BidDAO bidDAO, ItemDAO itemDAO) {
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.itemDAO = itemDAO;
    }

    public boolean createAuction(Auction auction, int userId) {
        Item item = itemDAO.findById(auction.getItemId());
        if (item == null) return false;
        if (item.getOwnerId() != userId) return false;
        auction.setStatus("UPCOMING");
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
        if ("FINISHED".equals(status) || "PAID".equals(status) || "CANCELED".equals(status)) {
            return false;
        }

        return auctionDAO.updateStatus(auctionId, "CANCELED");
    }

    /**
     * Syncs auction status with the current time.
     * UPCOMING → RUNNING → FINISHED (terminal states PAID/CANCELED are never overwritten).
     */
    public void refreshAuctionStatus(int auctionId) {
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return;

        String current = auction.getStatus();
        if ("PAID".equals(current) || "CANCELED".equals(current)) return;

        LocalDateTime now = LocalDateTime.now();
        String newStatus;
        if (now.isBefore(auction.getStartTime())) {
            newStatus = "UPCOMING";
        } else if (now.isBefore(auction.getEndTime())) {
            newStatus = "RUNNING";
        } else {
            newStatus = "FINISHED";
        }

        auctionDAO.updateStatus(auctionId, newStatus);
    }

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

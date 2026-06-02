package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.service.NotificationService;
import Server.websocket.BidWebSocketServer;
import Server.model.auction.Auction;
import Server.model.auction.Bid;
import Server.model.users.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public class BiddingService {

    private final DataSource dataSource;
    private final UserDAO userDAO;
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;

    private AutoBidConfigService autoBidConfigService;
    private NotificationService  notificationService;

    public BiddingService(DataSource ds, UserDAO u, AuctionDAO a, BidDAO b) {
        this.dataSource = ds;
        this.userDAO = u;
        this.auctionDAO = a;
        this.bidDAO = b;
    }

    public DataSource getDataSource() { return dataSource; }
    public BidDAO     getBidDAO()     { return bidDAO; }

    public AutoBidConfigService getAutoBidConfigService() {
        return autoBidConfigService;
    }

    public void setAutoBidConfigService(AutoBidConfigService autoBidConfigService) {
        this.autoBidConfigService = autoBidConfigService;
    }

    public void setNotificationService(NotificationService ns) {
        this.notificationService = ns;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }

    // summary of a user's activity on one auction — highest bid, how many times they bid, did they win
    public record BidHistoryEntry(Auction auction, double myHighestBid, int myBidCount, boolean won) {}

    public List<Auction> getAuctionsForBidder(int userId) {
        return auctionDAO.findByBidder(userId);
    }

    // all finished auctions this user bid on, newest first
    // "won" means their highest bid matches the final price
    public List<BidHistoryEntry> getBidHistoryForUser(int userId) {
        List<Auction> finished = auctionDAO.findByBidder(userId).stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
                .toList();

        List<BidHistoryEntry> result = new ArrayList<>();
        for (Auction a : finished) {
            double myHighestBid = bidDAO.getMaxBidByUser(userId, a.getId());
            int myBidCount      = bidDAO.getBidCountByUser(userId, a.getId());
            // on a finished auction, currentPrice is the winning price
            boolean won = myHighestBid > 0 && myHighestBid == a.getCurrentPrice();
            result.add(new BidHistoryEntry(a, myHighestBid, myBidCount, won));
        }
        return result;
    }

    public void placeBid(int userId, int auctionId, double amount) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // lock the row so two bids can't race each other
                Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
                if (auction == null) throw new RuntimeException("Auction not found");

                if (!"ACTIVE".equals(auction.getStatus())) {
                    throw new RuntimeException("Auction is not active");
                }

                if (auction.getEndTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                    throw new RuntimeException("Auction has ended");
                }

                if (amount <= auction.getCurrentPrice()) {
                    throw new RuntimeException("Bid must be higher than current price of " + auction.getCurrentPrice());
                }

                // read balance inside the transaction so we get a consistent snapshot
                User user = userDAO.findById(conn, userId);
                if (user == null) throw new RuntimeException("User not found");

                // only charge the gap above what they've already put in for this auction
                double previousBid = bidDAO.getMaxBidByUser(conn, userId, auctionId);
                double extra = amount - previousBid;
                if (extra <= 0) extra = amount; // shouldn't happen, but charge full if calc breaks

                if (user.getBalance() < extra) {
                    throw new RuntimeException(
                        String.format("Số dư không đủ. Bạn cần thêm %,.0f ₫ để đặt giá này.", extra - user.getBalance()));
                }

                // snapshot the current leader before we overwrite them
                Integer previousLeader = bidDAO.findHighestBidder(auctionId);

                // all three writes go in together — commit or rollback as one unit
                userDAO.updateBalance(conn, userId, user.getBalance() - extra);
                auctionDAO.updateCurrentPrice(conn, auctionId, amount);
                bidDAO.create(conn, userId, auctionId, amount);

                // anti-snipe: last-minute bid? give everyone 2 more minutes
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String newEndTimeIso = null;
                if (now.isAfter(auction.getEndTime().minusMinutes(2)) && now.isBefore(auction.getEndTime())) {
                    LocalDateTime resetTo = now.plusMinutes(2);
                    bidDAO.updateEndtime(conn, auctionId, resetTo);
                    newEndTimeIso = resetTo.toString();
                }

                conn.commit();

                // tell whoever just got outbid — outside the TX so it doesn't block
                if (previousLeader != null && previousLeader != userId && notificationService != null) {
                    notificationService.send(previousLeader,
                        String.format("📢 Bạn đã bị vượt giá trong phiên đấu giá #%d! Giá hiện tại: %,.0f ₫.",
                            auctionId, amount));
                }

                // push the new price (and new end time if anti-snipe fired) to everyone watching
                BidWebSocketServer.getInstance().broadcastBidUpdate(auctionId, amount, userId, amount, newEndTimeIso);

                if (autoBidConfigService != null)
                    autoBidConfigService.triggerAutoBidding(auctionId);

            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException(e.getMessage(), e);
            }

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // same as placeBid but called by the auto-bid bot inside an existing transaction
    public void placeBidInternal(Connection conn, int userId, int auctionId, double price) throws Exception {
        Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
        if (auction == null) throw new RuntimeException("Auction not found");

        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new RuntimeException("Auction is not active");
        }

        if (auction.getEndTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new RuntimeException("Auction has ended");
        }

        if (price <= auction.getCurrentPrice()) {
            throw new RuntimeException("Bid must be higher than current price of " + auction.getCurrentPrice());
        }

        User user = userDAO.findById(conn, userId);
        if (user == null) throw new RuntimeException("User not found");

        double previousBid = bidDAO.getMaxBidByUser(conn, userId, auctionId);
        double extra = price - previousBid;
        if (extra <= 0) extra = price; // safety fallback

        if (user.getBalance() < extra) {
            throw new RuntimeException("Insufficient balance for auto-bid");
        }

        userDAO.updateBalance(conn, userId, user.getBalance() - extra);
        auctionDAO.updateCurrentPrice(conn, auctionId, price);
        bidDAO.create(conn, userId, auctionId, price);

        // anti-snipe applies to bots too
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (now.isAfter(auction.getEndTime().minusMinutes(2)) && now.isBefore(auction.getEndTime())) {
            bidDAO.updateEndtime(conn, auctionId, now.plusMinutes(2));
        }
    }

    public double getCurrentPriceforUpdate(Connection conn, int auctionId) throws Exception {
        Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
        if (auction == null) {
            throw new RuntimeException("Không tìm thấy phiên đấu giá");
        }
        return auction.getCurrentPrice();
    }

    public List<Bid> getBidHistory(int auctionId) {
        return bidDAO.getBidsByAuction(auctionId);
    }

    public double getCurrentPrice(int auctionId) {
        try {
            Auction auction = auctionDAO.findById(auctionId);
            if (auction == null) {
                throw new RuntimeException("Không tìm thấy phiên đấu giá");
            }
            return auction.getCurrentPrice();
        } catch (RuntimeException e) {
            System.out.println("Lỗi: " + e.getMessage());
            throw e;
        }
    }
}

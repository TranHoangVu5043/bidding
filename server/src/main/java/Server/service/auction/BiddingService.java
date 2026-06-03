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

    public record BidHistoryEntry(Auction auction, double myHighestBid, int myBidCount, boolean won) {}

    public List<Auction> getAuctionsForBidder(int userId) {
        return auctionDAO.findByBidder(userId);
    }

    /**
     * Returns one {@link BidHistoryEntry} per FINISHED auction the user placed bids on.
     * "won" is true when the user's highest bid equals the auction's final price.
     */
    public List<BidHistoryEntry> getBidHistoryForUser(int userId) {
        List<Auction> finished = auctionDAO.findByBidder(userId).stream()
                .filter(a -> "FINISHED".equalsIgnoreCase(a.getStatus()))
                .sorted((a, b) -> b.getEndTime().compareTo(a.getEndTime()))
                .toList();

        List<BidHistoryEntry> result = new ArrayList<>();
        for (Auction a : finished) {
            double myHighestBid = bidDAO.getMaxBidByUser(userId, a.getId());
            int myBidCount      = bidDAO.getBidCountByUser(userId, a.getId());
            // For a finished auction currentPrice IS the winning bid
            boolean won = myHighestBid > 0 && myHighestBid == a.getCurrentPrice();
            result.add(new BidHistoryEntry(a, myHighestBid, myBidCount, won));
        }
        return result;
    }

    public double placeBid(int userId, int auctionId, double amount) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock the auction row for this transaction to prevent concurrent updates.
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

                // Read the bidder's current balance inside the same transaction.
                User user = userDAO.findById(conn, userId);
                if (user == null) throw new RuntimeException("User not found");

                // Remember previous leader BEFORE inserting the new bid
                Integer previousLeader = bidDAO.findHighestBidder(auctionId);
                if (previousLeader != null && previousLeader == userId){
                    throw new RuntimeException("Bạn đang là người giữ giá cao nhất trong phiên này rồi!");
                }

                // Only charge the increment above the user's existing highest bid on this auction.
                double previousBid = bidDAO.getMaxBidByUser(conn, userId, auctionId);
                double extra = amount - previousBid;
                if (extra <= 0) extra = amount; // safety: treat as fresh bid if calc is wrong

                if (user.getBalance() < extra) {
                    throw new RuntimeException(
                        String.format("Số dư không đủ. Bạn cần thêm %,.0f ₫ để đặt giá này.", extra - user.getBalance()));
                }

                // Charge only the increment above the user's existing highest bid.
                double newBalance = user.getBalance() - extra;
                userDAO.updateBalance(conn, userId, newBalance);
                auctionDAO.updateCurrentPrice(conn, auctionId, amount);
                bidDAO.create(conn, userId, auctionId, amount);

                // Anti-sniping: if < 2 minutes remain, reset the timer to exactly now + 2 minutes
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                String newEndTimeIso = null;
                if (now.isAfter(auction.getEndTime().minusMinutes(2)) && now.isBefore(auction.getEndTime())) {
                    LocalDateTime resetTo = now.plusMinutes(2);
                    bidDAO.updateEndtime(conn, auctionId, resetTo);
                    newEndTimeIso = resetTo.toString();
                }

                conn.commit();

                // Notifications and auto-bid are fire-and-forget — don't block the HTTP response
                final Integer leader = previousLeader;
                final String endTime = newEndTimeIso;
                new Thread(() -> {
                    if (leader != null && leader != userId && notificationService != null) {
                        notificationService.send(leader,
                            String.format("📢 Bạn đã bị vượt giá trong phiên đấu giá #%d! Giá hiện tại: %,.0f ₫.",
                                auctionId, amount));
                    }
                    if (notificationService != null) {
                        notificationService.send(userId,
                            String.format("🎉 Tuyệt vời! Bạn đã đè giá thành công trong phiên #%d! Bạn đang dẫn đầu với mức giá: %,.0f ₫.",
                                auctionId, amount));
                    }
                }).start();

                BidWebSocketServer.getInstance().broadcastBidUpdate(auctionId, amount, userId, amount, endTime);

                if (autoBidConfigService != null) {
                    new Thread(() -> autoBidConfigService.triggerAutoBidding(auctionId)).start();
                }

                return newBalance;

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
    //Dành cho bot
    public void placeBidInternal(Connection conn, int userId, int auctionId, double price) throws Exception {
        // Lock the auction row for this transaction to prevent concurrent updates.
        Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
        if (auction == null) throw new RuntimeException("Auction not found");

        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new RuntimeException("Auction is not active");
        }

        if (auction.getEndTime().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new RuntimeException("Auction has ended");
        }
        Integer previousLeader = bidDAO.findHighestBidder(auctionId);

        if (previousLeader== null) {
            // Nếu chưa có ai mở bát, Bot được quyền đặt BẰNG giá khởi điểm
            if (price < auction.getCurrentPrice()) {
                throw new RuntimeException("Giá đặt phải lớn hơn hoặc bằng giá khởi điểm: " + auction.getCurrentPrice());
            }
        } else {
            // Nếu đã có người dẫn đầu rồi, lượt đặt tiếp theo BẮT BUỘC phải lớn hơn tuyệt đối
            if (price <= auction.getCurrentPrice()) {
                throw new RuntimeException("Giá đặt phải lớn hơn giá hiện tại của " + auction.getCurrentPrice());
            }
            if (previousLeader != userId) {
                User oldLeader = userDAO.findById(conn, previousLeader);
                if (oldLeader != null) {
                    double oldLeaderBid = bidDAO.getMaxBidByUser(conn, previousLeader, auctionId);
                    userDAO.updateBalance(conn, previousLeader, oldLeader.getBalance() + oldLeaderBid);
                }
            }
        }

        // Read the bidder's current balance inside the same transaction.
        User user = userDAO.findById(conn, userId);
        if (user == null) throw new RuntimeException("User not found");

        // Only charge the increment above the user's existing highest bid on this auction.
        double previousBid = bidDAO.getMaxBidByUser(conn, userId, auctionId);
        double extra = price - previousBid;
        if (extra <= 0) extra = price; // safety fallback

        if (user.getBalance() < extra) {
            throw new RuntimeException("Insufficient balance for auto-bid");
        }

        // All three writes share the same connection and will commit or rollback together.
        userDAO.updateBalance(conn, userId, user.getBalance() - extra);
        auctionDAO.updateCurrentPrice(conn, auctionId, price);
        bidDAO.create(conn, userId, auctionId, price);

        // anti-snipe applies to bots too
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        if (now.isAfter(auction.getEndTime().minusMinutes(2)) && now.isBefore(auction.getEndTime())) {
            bidDAO.updateEndtime(conn, auctionId, now.plusMinutes(2));
        }
    }
    public double getCurrentPriceforUpdate(Connection conn, int auctionId) throws Exception{
        Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
        if (auction == null) {
            throw new RuntimeException("Không tìm thấy phiên đấu giá");
        }
        return auction.getCurrentPrice();


    }
    public List<Bid> getBidHistory(int auctionId) {
        return bidDAO.getBidsByAuction(auctionId);
    }

    public double getCurrentPrice(int auctionId){
        try {
            Auction auction = auctionDAO.findById(auctionId);
            if (auction == null){
                throw  new RuntimeException("Không tìm thấy phiên đấu giá");
            }
            return auction.getCurrentPrice();
        } catch (RuntimeException e) {
            throw e;
        }
    }
    public UserDAO getUserDAO() {
        return userDAO;
    }
}

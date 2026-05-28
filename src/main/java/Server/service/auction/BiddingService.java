package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.websocket.BidWebSocketServer;
import Server.model.auction.Auction;
import Server.model.auction.Bid;
import Server.model.users.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.List;

public class BiddingService {

    private final DataSource dataSource;
    private final UserDAO userDAO;
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;

    private AutoBidConfigService autoBidConfigService;

    public BiddingService(DataSource ds, UserDAO u, AuctionDAO a, BidDAO b) {
        this.dataSource = ds;
        this.userDAO = u;
        this.auctionDAO = a;
        this.bidDAO = b;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public AutoBidConfigService getAutoBidConfigService() {
        return autoBidConfigService;
    }

    public void setAutoBidConfigService(AutoBidConfigService autoBidConfigService) {
        this.autoBidConfigService = autoBidConfigService;
    }

    public List<Auction> getAuctionsForBidder(int userId) {
        return auctionDAO.findByBidder(userId);
    }

    public void placeBid(int userId, int auctionId, double amount) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock the auction row for this transaction to prevent concurrent updates.
                Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
                if (auction == null) throw new RuntimeException("Auction not found");

                if (!"ACTIVE".equals(auction.getStatus())) {
                    throw new RuntimeException("Auction is not active");
                }

                if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Auction has ended");
                }

                if (amount <= auction.getCurrentPrice()) {
                    throw new RuntimeException("Bid must be higher than current price of " + auction.getCurrentPrice());
                }

                // Read the bidder's current balance inside the same transaction.
                User user = userDAO.findById(conn, userId);
                if (user == null) throw new RuntimeException("User not found");

                if (user.getBalance() < amount) {
                    throw new RuntimeException("Insufficient balance");
                }

                // All three writes share the same connection and will commit or rollback together.
                userDAO.updateBalance(conn, userId, user.getBalance() - amount);
                auctionDAO.updateCurrentPrice(conn, auctionId, amount);
                bidDAO.create(conn, userId, auctionId, amount);

                //snipping
                LocalDateTime now = LocalDateTime.now();
                long finalminutes = 5;
                long extendminutes = 3;
                if (now.isAfter(auction.getEndTime().minusMinutes(finalminutes)) && now.isBefore(auction.getEndTime())){
                    LocalDateTime newEndtime = auction.getEndTime().plusMinutes(extendminutes);
                    bidDAO.updateEndtime(conn, auctionId, newEndtime);
                }

                conn.commit();

                // Broadcast real-time update to all WebSocket subscribers
                BidWebSocketServer.getInstance().broadcastBidUpdate(auctionId, amount, userId, amount);

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
    //Dành cho bot
    public void placeBidInternal(Connection conn, int userId, int auctionId, double price) throws Exception {
        // Lock the auction row for this transaction to prevent concurrent updates.
        Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
        if (auction == null) throw new RuntimeException("Auction not found");

        if (!"ACTIVE".equals(auction.getStatus())) {
            throw new RuntimeException("Auction is not active");
        }

        if (auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Auction has ended");
        }

        if (price <= auction.getCurrentPrice()) {
            throw new RuntimeException("Bid must be higher than current price of " + auction.getCurrentPrice());
        }

        // Read the bidder's current balance inside the same transaction.
        User user = userDAO.findById(conn, userId);
        if (user == null) throw new RuntimeException("User not found");

        if (user.getBalance() < price) {
            throw new RuntimeException("Insufficient balance for Bot ");
        }

        // All three writes share the same connection and will commit or rollback together.
        userDAO.updateBalance(conn, userId, user.getBalance() - price);
        auctionDAO.updateCurrentPrice(conn, auctionId, price);
        bidDAO.create(conn, userId, auctionId, price);

        //snipping
        LocalDateTime now = LocalDateTime.now();
        long finalminutes = 5;
        long extendminutes = 3;
        if (now.isAfter(auction.getEndTime().minusMinutes(finalminutes)) && now.isBefore(auction.getEndTime())) {
            LocalDateTime newEndtime = auction.getEndTime().plusMinutes(extendminutes);
            bidDAO.updateEndtime(conn, auctionId, newEndtime);
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
        }catch (RuntimeException e){
            System.out.println("Lỗi: " + e.getMessage());
            throw e;
        }
    }
}

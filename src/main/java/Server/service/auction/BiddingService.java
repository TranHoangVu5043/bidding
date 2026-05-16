package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
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

    public BiddingService(DataSource ds, UserDAO u, AuctionDAO a, BidDAO b) {
        this.dataSource = ds;
        this.userDAO = u;
        this.auctionDAO = a;
        this.bidDAO = b;
    }

    public void placeBid(int userId, int auctionId, double amount) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lock the auction row for this transaction to prevent concurrent updates.
                Auction auction = auctionDAO.findByIdForUpdate(conn, auctionId);
                if (auction == null) throw new RuntimeException("Auction not found");

                if (!"RUNNING".equals(auction.getStatus())) {
                    throw new RuntimeException("Auction is not running");
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

                conn.commit();

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

    public List<Bid> getBidHistory(int auctionId) {
        return bidDAO.getBidsByAuction(auctionId);
    }
}

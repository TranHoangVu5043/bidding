package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.users.UserDAO;
import Server.model.auction.Auction;
import Server.model.users.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;

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
                User user = userDAO.findById(userId);
                Auction auction = auctionDAO.findById(auctionId);

                if (!auction.getStatus().equals("ACTIVE")) {
                    throw new RuntimeException("Auction not active");
                }

                if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                    throw new RuntimeException("Auction ended");
                }

                if (amount <= auction.getCurrentPrice()) {
                    throw new RuntimeException("Bid too low");
                }

                if (user.getBalance() < amount) {
                    throw new RuntimeException("Insufficient balance");
                }

                userDAO.updateBalance(userId, user.getBalance() - amount);
                auctionDAO.updateCurrentPrice(auctionId, amount);
                bidDAO.create(conn, userId, auctionId, amount);

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            System.err.println("[ERROR] placeBid: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
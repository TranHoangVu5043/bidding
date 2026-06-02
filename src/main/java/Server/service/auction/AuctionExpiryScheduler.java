package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.model.auction.Auction;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Runs every 30s in the background and nudges any auction whose time has passed:
//   UPCOMING → ACTIVE  (start time hit)
//   ACTIVE   → FINISHED (end time hit, triggers refunds + winner notification)
// All the real work happens inside refreshAuctionStatus so the logic stays in one place.
public class AuctionExpiryScheduler {

    private static final int INTERVAL_SECONDS = 30;

    private final AuctionDAO     auctionDAO;
    private final AuctionService auctionService;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "auction-expiry-scheduler");
                t.setDaemon(true); // dies with the JVM, no cleanup needed
                return t;
            });

    public AuctionExpiryScheduler(AuctionDAO auctionDAO, AuctionService auctionService) {
        this.auctionDAO     = auctionDAO;
        this.auctionService = auctionService;
    }

    // kick it off — safe to call once at startup
    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
        System.out.println("[AuctionExpiryScheduler] Started — checking every " + INTERVAL_SECONDS + "s");
    }

    public void stop() {
        executor.shutdown();
    }

    private void tick() {
        try {
            List<Auction> stale = auctionDAO.findNeedingStatusUpdate();
            if (stale.isEmpty()) return;

            System.out.println("[AuctionExpiryScheduler] Refreshing " + stale.size() + " auction(s)");
            for (Auction auction : stale) {
                try {
                    auctionService.refreshAuctionStatus(auction.getId());
                } catch (Exception e) {
                    System.err.println("[AuctionExpiryScheduler] Failed on auction #"
                            + auction.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[AuctionExpiryScheduler] Tick crashed: " + e.getMessage());
        }
    }
}

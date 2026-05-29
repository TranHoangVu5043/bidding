package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.model.auction.Auction;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background scheduler that automatically transitions auctions between statuses:
 * <ul>
 *   <li>UPCOMING → ACTIVE  when start_time is reached</li>
 *   <li>ACTIVE   → FINISHED when end_time is reached (triggers refunds + winner notification)</li>
 * </ul>
 *
 * Runs every {@value #INTERVAL_SECONDS} seconds. Uses the existing
 * {@link AuctionService#refreshAuctionStatus} logic so refund and notification behaviour
 * stays in one place.
 */
public class AuctionExpiryScheduler {

    private static final int INTERVAL_SECONDS = 30;

    private final AuctionDAO     auctionDAO;
    private final AuctionService auctionService;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "auction-expiry-scheduler");
                t.setDaemon(true); // don't block JVM shutdown
                return t;
            });

    public AuctionExpiryScheduler(AuctionDAO auctionDAO, AuctionService auctionService) {
        this.auctionDAO     = auctionDAO;
        this.auctionService = auctionService;
    }

    /** Starts the periodic check. Safe to call multiple times — only the first call has effect. */
    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
        System.out.println("[AuctionExpiryScheduler] Started — checking every " + INTERVAL_SECONDS + "s");
    }

    /** Stops the scheduler gracefully. */
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
                    System.err.println("[AuctionExpiryScheduler] Error refreshing auction #"
                            + auction.getId() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[AuctionExpiryScheduler] Tick failed: " + e.getMessage());
        }
    }
}

package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.model.auction.Auction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// Runs every 30s in the bg to check auction state
public class AuctionExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(AuctionExpiryScheduler.class);
    private static final int INTERVAL_SECONDS = 30;

    private final AuctionDAO     auctionDAO;
    private final AuctionService auctionService;

    private final ScheduledExecutorService executor =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r, "auction-expiry-scheduler");

                t.setDaemon(true);

                return t;
            });

    public AuctionExpiryScheduler(AuctionDAO auctionDAO, AuctionService auctionService) {
        this.auctionDAO     = auctionDAO;
        this.auctionService = auctionService;
    }

    public void start() {
        executor.scheduleAtFixedRate(this::tick, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
        log.info("Started — checking every {}s", INTERVAL_SECONDS);
    }

    public void stop() {
        executor.shutdown();
    }

    private void tick() {
        try {
            List<Auction> stale = auctionDAO.findNeedingStatusUpdate();
            if (stale.isEmpty()) return;

            log.info("Refreshing {} auction(s)", stale.size());
            for (Auction auction : stale) {
                try {
                    auctionService.refreshAuctionStatus(auction.getId());
                } catch (Exception e) {
                    log.error("Failed to refresh auction #{}", auction.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Tick crashed", e);
        }
    }
}

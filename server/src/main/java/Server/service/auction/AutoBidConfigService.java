package Server.service.auction;

import Server.model.auction.AutoBidConfig;
import Server.model.users.User;
import Server.websocket.BidWebSocketServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBidConfigService {

    private static final Logger log = LoggerFactory.getLogger(AutoBidConfigService.class);

    private final Map<Integer, PriorityQueue<AutoBidConfig>> autoBidmaps = new ConcurrentHashMap<>();
    private final BiddingService biddingService;

    public  AutoBidConfigService (BiddingService biddingService){
        this.biddingService = biddingService;
    }

    public synchronized void registerAutoBid(int auctionId, int userId, double maxBid, double increment){
        try (Connection conn = biddingService.getDataSource().getConnection()) {
            User user = biddingService.getUserDAO().findById(conn, userId);
            double balance = user.getBalance();
            if (maxBid > balance) {
                throw new RuntimeException(String.format(
                        "Cài đặt Bot thất bại! Hạn mức tối đa của Bot (%,.0f ₫) không được vượt quá số tiền hiện có trong ví của bạn (%,.0f ₫).",
                        maxBid, balance));
            }
            double currentPrice = biddingService.getCurrentPriceforUpdate(conn, auctionId);
            if (maxBid <= currentPrice) {
                throw new RuntimeException("Hạn mức tối đa của Bot phải lớn hơn giá hiện tại của phòng đấu giá!");
            }
            Integer currentLeader = biddingService.getBidDAO().findHighestBidder(auctionId);
            if (currentLeader != null && currentLeader == userId) {
                throw new RuntimeException("Bạn đang là người dẫn đầu phiên đấu giá! Không thể cấu hình lại Bot lúc này.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        AutoBidConfig autoBid = new AutoBidConfig(auctionId, userId, maxBid, increment);

        autoBidmaps.putIfAbsent(auctionId, new PriorityQueue<>());
        autoBidmaps.get(auctionId).removeIf(config -> config.getUserId() == userId);
        autoBidmaps.get(auctionId).add(autoBid);
        if (biddingService.getNotificationService() != null) {
            biddingService.getNotificationService().send(userId,
                    String.format("🤖 Kích hoạt Bot tự động thành công cho phiên #%d! Hạn mức: %,.0f ₫ | Bước nhảy: %,.0f ₫.",
                            auctionId, maxBid, increment));
        }

        // Run async so the HTTP response returns immediately (~300ms) instead of
        // blocking for 10+ sequential DB round-trips (~4s).
        int aid = auctionId;
        new Thread(() -> triggerAutoBidding(aid), "autobid-trigger-" + auctionId).start();

    }
    public synchronized void triggerAutoBidding(int auctionId){
        PriorityQueue<AutoBidConfig> queue = autoBidmaps.get(auctionId);

        if (queue == null || queue.isEmpty()) return;

        try (Connection conn = biddingService.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                //get current leader for noti
                Integer previousLeader = biddingService.getBidDAO().findHighestBidder(auctionId);

                double currentPrice = biddingService.getCurrentPriceforUpdate(conn, auctionId);
                AutoBidConfig top1 = queue.peek();

                if (top1 == null) {
                    conn.commit();
                    return;
                }
                if (previousLeader != null && previousLeader == top1.getUserId()) {
                    // top1 is already the leader. Give the next challenger a chance to place
                    // their opening bid, then top1 will auto-respond in the next round.

                    AutoBidConfig savedTop1 = queue.poll();       // temporarily remove top1
                    AutoBidConfig challenger = queue.peek();       // peek at second-best (stays in queue)
                    queue.add(savedTop1);                         // restore top1

                    if (challenger == null || challenger.getMaxBid() <= currentPrice) {
                        conn.commit();
                        return; // no viable challenger — leader is unchallenged
                    }

                    double challengePrice = Math.min(
                            currentPrice + challenger.getIncrement(), challenger.getMaxBid());
                    if (challengePrice <= currentPrice) {
                        conn.commit();
                        return;
                    }

                    biddingService.placeBidInternal(conn, challenger.getUserId(), auctionId, challengePrice);
                    conn.commit();
                    BidWebSocketServer.getInstance().broadcastBidUpdate(
                            auctionId, challengePrice, challenger.getUserId(), challengePrice, null);

                    // top1 now responds to the challenge
                    triggerAutoBidding(auctionId);
                    return;
                }
                AutoBidConfig top1Config = queue.poll();
                AutoBidConfig top2Config = queue.poll();
                if (top1Config == null || top1Config.getMaxBid() <= currentPrice) {
                    conn.commit(); //release cn
                    return;
                }

                double finalPrice = currentPrice;
                int winnerBotId = -1;

                if (top1Config.getMaxBid() <= finalPrice) return;

                else if (top2Config == null) { // only 1 auto-bidder
                    winnerBotId = top1Config.getUserId();

                    queue.add(top1Config);

                    if (previousLeader == null){
                        finalPrice = currentPrice;
                    }else {
                        finalPrice = Math.min(currentPrice + top1Config.getIncrement(), top1Config.getMaxBid());
                    }

                } else if (top1Config.getMaxBid() == top2Config.getMaxBid()) {

                    finalPrice = top1Config.getMaxBid();
                    winnerBotId = top1Config.getUserId();

                    queue.add(top1Config);

                } else {
                    double max2 = top2Config.getMaxBid();
                    double inc1 = top1Config.getIncrement();
                    double inc2 = top2Config.getIncrement();
                    double totalIncrement = inc1 + inc2;
                    double buggetLeftB = max2 - currentPrice;

                    long loop = (long) (buggetLeftB / totalIncrement);
                    finalPrice = currentPrice + loop * totalIncrement;

                    if (finalPrice + inc2 <= max2) {
                        finalPrice += inc2;
                    } else {
                        finalPrice = max2;
                    }

                    finalPrice = Math.min(finalPrice + inc1, top1Config.getMaxBid());

                    winnerBotId = top1Config.getUserId();

                    queue.add(top1Config);
                }

                if (winnerBotId != -1 && ( finalPrice > currentPrice||previousLeader == null)) {
                    biddingService.placeBidInternal(conn, winnerBotId, auctionId, finalPrice);
                }

                conn.commit();

                if (winnerBotId != -1 && (finalPrice > currentPrice || previousLeader == null)) {
                    log.info("Auto-bid fired — auction #{} winner bot user#{} at price {}", auctionId, winnerBotId, finalPrice);

                    // Broadcast to all clients so their UI refreshes price + balance
                    BidWebSocketServer.getInstance().broadcastBidUpdate(
                            auctionId, finalPrice, winnerBotId, finalPrice, null);

                    if (biddingService.getNotificationService() != null) {
                        if (previousLeader != null && previousLeader != winnerBotId) {
                            biddingService.getNotificationService().send(previousLeader,
                                String.format("📢 Bạn đã bị vượt giá (tự động) trong phiên đấu giá #%d! Giá hiện tại: %,.0f ₫.",
                                    auctionId, finalPrice));
                        }
                        biddingService.getNotificationService().send(winnerBotId,
                            String.format("🤖 Hệ thống Auto-bid của bạn đã tự động đè giá thành công trong phiên #%d! Giá hiện tại: %,.0f ₫.",
                                auctionId, finalPrice));
                    }
                }
            }catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Auto-bid coordinator failed for auction #{}", auctionId, e);
        }
    }
}

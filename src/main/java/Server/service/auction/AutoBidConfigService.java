package Server.service.auction;

import Server.model.auction.AutoBidConfig;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
public class AutoBidConfigService {

    private final Map<Integer, PriorityQueue<AutoBidConfig>> autoBidmaps = new ConcurrentHashMap<>();
    private final BiddingService biddingService;

    public  AutoBidConfigService (BiddingService biddingService){
        this.biddingService = biddingService;
    }

    public synchronized void registerAutoBid(int auctionId, int userId, double maxBid, double increment){
        AutoBidConfig autoBid = new AutoBidConfig(auctionId, userId, maxBid, increment);

        autoBidmaps.putIfAbsent(auctionId, new PriorityQueue<>());
        autoBidmaps.get(auctionId).removeIf(config -> config.getUserId() == userId);
        autoBidmaps.get(auctionId).add(autoBid);

    }
    public synchronized void triggerAutoBidding(int auctionId){
        PriorityQueue<AutoBidConfig> queue = autoBidmaps.get(auctionId);
        if (queue == null || queue.isEmpty()) return;

        try (Connection conn = biddingService.getDataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Capture the current leader before auto-bid fires (for outbid notification)
                Integer previousLeader = biddingService.getBidDAO().findHighestBidder(auctionId);

                double currentPrice = biddingService.getCurrentPriceforUpdate(conn, auctionId);
                AutoBidConfig top1Config = queue.poll();
                AutoBidConfig top2Config = queue.poll();
                if (top1Config == null || top1Config.getMaxBid() <= currentPrice) {
                    conn.commit(); // Giải phóng nếu Bot không cần chạy
                    return;
                }

                double finalPrice = currentPrice;
                int winnerBotId = -1;

                if (top1Config.getMaxBid() <= finalPrice) return;

                else if (top2Config == null) {
                    winnerBotId = top1Config.getUserId();
                    finalPrice = Math.min(currentPrice + top1Config.getIncrement(), top1Config.getMaxBid());
                    queue.add(top1Config);
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
                if (winnerBotId != -1 && finalPrice > currentPrice) {
                    biddingService.placeBidInternal(conn, winnerBotId, auctionId, finalPrice);
                }
                conn.commit();
                if (winnerBotId != -1 && finalPrice > currentPrice) {
                    System.out.println("BotWinner: " + winnerBotId + " || FinalPrice: " + finalPrice);
                    // Notify the person the auto-bid just outbid
                    if (previousLeader != null && previousLeader != winnerBotId
                            && biddingService.getNotificationService() != null) {
                        double fp = finalPrice;
                        biddingService.getNotificationService().send(previousLeader,
                            String.format("📢 Bạn đã bị vượt giá (tự động) trong phiên đấu giá #%d! Giá hiện tại: %,.0f ₫.",
                                auctionId, fp));
                    }
                }
            }catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println(" Lỗi hệ thống điều phối Bot: " + e.getMessage());
        }
    }
}

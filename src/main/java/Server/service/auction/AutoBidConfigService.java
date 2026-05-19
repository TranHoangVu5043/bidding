package Server.service.auction;

import Server.model.auction.AutoBidConfig;

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

        autoBidmaps.get(auctionId).add(autoBid);

    }
    public synchronized void triggerAutoBidding(int auctionId){
        PriorityQueue<AutoBidConfig> queue = autoBidmaps.get(auctionId);

        if (queue == null || queue.isEmpty()) return;

        double currentPrice = getCurrentPriceFromDB(auctionId);

        AutoBidConfig top1Config = queue.poll();
        AutoBidConfig top2Config = queue.poll(); //có thể null

        double max2 = top2Config.getMaxBid();
        double inc1 = top1Config.getIncrement();
        double inc2 = top2Config.getIncrement();
        double totalIncrement = inc1 + inc2;
        double buggetLeftB = max2 - currentPrice;
        double loop = buggetLeftB / totalIncrement;

        double finalPrice = currentPrice;
        int winnerBotId = -1;

        if (top1Config.getMaxBid()< finalPrice) return;

        else if (top2Config == null) {
            winnerBotId = top1Config.getUserId();
            finalPrice = Math.min(currentPrice + inc1, top1Config.getMaxBid());
            queue.add(top1Config);
        }
        else if (top1Config.getMaxBid()==top2Config.getMaxBid()){
            finalPrice = top1Config.getMaxBid();
            if (loop*totalIncrement == buggetLeftB){
                winnerBotId = top2Config.getUserId();
                queue.add(top2Config);
            }
            else {
                winnerBotId = top1Config.getUserId();
                queue.add(top1Config);
            }
        }
        else{
            finalPrice = currentPrice + loop * totalIncrement + inc1;
            winnerBotId = top1Config.getUserId();
            queue.add(top1Config);
        }
        if (winnerBotId != -1 && finalPrice > currentPrice){
            try {
                biddingService.placeBidInternal(winnerBotId, auctionId, finalPrice);
                System.out.println("BotWinner: "  + winnerBotId  + " || FinalPrice: " + finalPrice);
            }catch (Exception e){
                System.out.println("Lỗi cập nhật giá" + e.getMessage());
            }
        }
    }
    private double getCurrentPriceFromDB(int auctionId){
        return biddingService.getCurrentPrice(auctionId);

    }
}

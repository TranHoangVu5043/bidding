package Server.model.auction;

import java.time.LocalDateTime;
import java.util.PriorityQueue;

public class AutoBidConfig implements Comparable<AutoBidConfig>{
    private int auctionId;
    private int userId;
    private double maxBid;
    private double increment;
    private LocalDateTime createTime;

    public AutoBidConfig(int auctionId,
                         int userId,
                         double maxBid,
                         double increment,
                         LocalDateTime createTime){
        this.auctionId = auctionId;
        this.userId = userId;
        this.maxBid = maxBid;
        this.increment = maxBid;
        this.createTime = createTime;
    }

    public int getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(int auctionId) {
        this.auctionId = auctionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public double getMaxBid() {
        return maxBid;
    }

    public void setMaxBid(double maxBid) {
        this.maxBid = maxBid;
    }

    public double getIncrement() {
        return increment;
    }

    public void setIncrement(double increment) {
        this.increment = increment;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public int compareTo(AutoBidConfig other){
        if (this.maxBid != other.maxBid){
            return Double.compare(other.maxBid, this.maxBid);
        }
        return this.createTime.compareTo(other.createTime);
    }
}

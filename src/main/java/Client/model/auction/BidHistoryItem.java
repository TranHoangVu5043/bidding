package Client.model.auction;

public class BidHistoryItem {

    private Auction auction;
    private double  myHighestBid;
    private int     myBidCount;
    private boolean won;

    public Auction getAuction()      { return auction; }
    public double  getMyHighestBid() { return myHighestBid; }
    public int     getMyBidCount()   { return myBidCount; }
    public boolean isWon()           { return won; }

    public void setAuction(Auction auction)           { this.auction      = auction; }
    public void setMyHighestBid(double myHighestBid)  { this.myHighestBid = myHighestBid; }
    public void setMyBidCount(int myBidCount)         { this.myBidCount   = myBidCount; }
    public void setWon(boolean won)                   { this.won          = won; }
}

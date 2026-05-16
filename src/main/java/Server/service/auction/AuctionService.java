package Server.service.auction;

import Server.dao.auction.AuctionDAO;
import Server.dao.auction.BidDAO;
import Server.dao.auction.ItemDAO;
import Server.model.auction.Auction;
import Server.model.auction.items.Item;

import java.time.LocalDateTime;

public class AuctionService {
    private final AuctionDAO auctionDAO;
    private final BidDAO bidDAO;
    private final ItemDAO itemDAO;
    public AuctionService(AuctionDAO auctionDAO, BidDAO bidDAO, ItemDAO itemDAO){
        this.auctionDAO = auctionDAO;
        this.bidDAO = bidDAO;
        this.itemDAO = itemDAO;
    }
    //A.quản lsi cuộc đấu giá
    //Tạo cuoc dau gia moi
    public boolean createAuction(Auction auction, int usedId){
        Item item = itemDAO.findById(auction.getItemId());
        if (item == null) return false;
        if (item.getOwnerId() != usedId) return false;
        return auctionDAO.create(auction);
    }
    //huy dau gia
    public boolean cancelAuction(int auctionId, int userId){
        Auction auction = auctionDAO.findById(auctionId);
        if (auction == null) return false;
        if (auction.getOwnerId() != userId) return false;
        if (LocalDateTime.now().isBefore(auction.getEndTime())) return false;
        return auctionDAO.updateStatus(auctionId, "CANCELED");
    }
    //thay doi trang thai vao database
    public void refeshAuctionStatus(int auctionId){
        Auction auction = auctionDAO.findById(auctionId);
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getStartTime())){
            auctionDAO.updateStatus(auctionId,"UPCOMING");
        }
        else if (now.equals(auction.getStartTime())){
            auctionDAO.updateStatus(auctionId,"OPEN");
        }
        else if (now.isAfter(auction.getStartTime()) && now.isBefore(auction.getEndTime())){
            auctionDAO.updateStatus(auctionId,"RUNNING");
        }
        else if (now.equals(auction.getEndTime())){
            auctionDAO.updateStatus(auctionId,"FINISHED");
        }
    }

}

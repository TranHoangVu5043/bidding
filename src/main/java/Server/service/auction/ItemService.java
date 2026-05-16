package Server.service.auction;

import Server.dao.auction.ItemDAO;
import Server.model.auction.items.Item;

import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO;
    public ItemService(ItemDAO itemDAO){
        this.itemDAO = itemDAO;
    }

    public boolean addItem(Item item){
        if(item.getName() == null || item.getName().trim().isEmpty()){
            return false;
        }
        itemDAO.create(item);
        return true;
    }

    public boolean deleteItem(int itemId){
        Item existingItem = itemDAO.findById(itemId);
        if(existingItem == null){
            return false;
        }
        itemDAO.delete(itemId);
        return true;
    }

    public boolean updateItem(Item item){
        if(itemDAO.findById(item.getId()) == null){
            System.err.println("Lỗi: Không tìm thấ sản phẩm");
            return false;
        }
        itemDAO.update(item);
        return true;
    }

    public Item getItemDetail(int itemId){
        return itemDAO.findById(itemId);
    }

    public List<Item> getItemsByOwner(int ownerId) {
        return itemDAO.findByOwnerId(ownerId);
    }

    public boolean canModifyItem(int itemId, int currentUserId) {
        Item item = itemDAO.findById(itemId);
        if (item == null) return false;
        return item.getOwnerId() == currentUserId;
    }

}
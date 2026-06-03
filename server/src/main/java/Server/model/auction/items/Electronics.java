package Server.model.auction.items;

public class Electronics extends Item {

    public Electronics(int id, String name, String description, int ownerId,
                       String category, String condition, double price, int stock) {
        super(id, name, description, ownerId, category, condition, price, stock);
    }

    @Override
    public void displayInfo() {
        System.out.println("[Electronics] " + getName() + " | Condition: " + getCondition());
    }
}

package Server.model.auction.items;

public class Electronics extends Item {
    private String warrantyPeriod;
    private double weight;

    public String getWarrantyPeriod() {
        return warrantyPeriod;
    }

    public void setWarrantyPeriod(String warrantyPeriod) {
        this.warrantyPeriod = warrantyPeriod;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public Electronics(String id, String name, String description, int ownerId, String category, String condition
            , String warrantyPeriod, double weight){
        super(id, name, description, ownerId, category, condition);
        this.warrantyPeriod = warrantyPeriod;
        this.weight = weight;
    }
}

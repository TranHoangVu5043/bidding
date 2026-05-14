package Server.model.auction.items;


import Server.model.Entity;

public abstract class Item implements Entity {
    private int id;
    private String name;
    private String description;//mieu ta san pham
    private String condition;
    private int ownerId;
    private String category;
    private String status;

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Item(int id, String name, String description, int ownerId, String category, String condition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.ownerId= ownerId;
        this.category= category;
        this.condition= condition;
    }

    public void displayInfo() {
        System.out.println(id + " " + name);
    }
}

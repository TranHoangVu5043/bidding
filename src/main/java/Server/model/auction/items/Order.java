package Server.model.auction.items;


import java.time.LocalDateTime;

public class Order {
    private long id;
    private int sellerId;
    private int buyerId;
    private String productName;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public Order() {}

    public Order(long id, int sellerId, int buyerId, String productName, double totalAmount, String status, LocalDateTime createdAt) {
        this.id = id;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getSellerId() { return sellerId; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

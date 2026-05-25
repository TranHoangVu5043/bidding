package Client.model.auction;

public class Order {
    private long id;
    private String productName;
    private double totalAmount;
    private String status;
    private String date ;

    public Order() {}

    public Order(long id, String productName, double totalAmount, String status,String date) {
        this.id = id;
        this.productName = productName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.date = date ;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
}
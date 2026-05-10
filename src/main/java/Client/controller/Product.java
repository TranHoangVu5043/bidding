package application;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Product {
    private final SimpleStringProperty name;
    private final SimpleDoubleProperty price;
    private final SimpleIntegerProperty stock;
    private final SimpleStringProperty status;

    public Product(String name, double price, int stock, String status) {
        this.name = new SimpleStringProperty(name);
        this.price = new SimpleDoubleProperty(price);
        this.stock = new SimpleIntegerProperty(stock);
        this.status = new SimpleStringProperty(status);
    }

    public String getName() { return name.get(); }
    public double getPrice() { return price.get(); }
    public int getStock() { return stock.get(); }
    public String getStatus() { return status.get(); }

    public SimpleStringProperty nameProperty() { return name; }
    public SimpleDoubleProperty priceProperty() { return price; }
    public SimpleIntegerProperty stockProperty() { return stock; }
    public SimpleStringProperty statusProperty() { return status; }

    public void setName(String name) { this.name.set(name); }
    public void setPrice(double price) { this.price.set(price); }
    public void setStock(int stock) { this.stock.set(stock); }
    public void setStatus(String status) { this.status.set(status); }
}


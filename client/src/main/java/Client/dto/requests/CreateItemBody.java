package Client.dto.requests;

public class CreateItemBody {
    public final String name, description, category, condition;
    public final double price;
    public final int    stock;

    public CreateItemBody(String name, String description, String category,
                          String condition, double price, int stock) {
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.condition   = condition;
        this.price       = price;
        this.stock       = stock;
    }
}

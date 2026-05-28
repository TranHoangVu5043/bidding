package Client.dto.requests;

public class UpdateItemBody {
    public final int    itemId;
    public final String name, description, category, condition;

    public UpdateItemBody(int itemId, String name, String description,
                          String category, String condition) {
        this.itemId      = itemId;
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.condition   = condition;
    }
}

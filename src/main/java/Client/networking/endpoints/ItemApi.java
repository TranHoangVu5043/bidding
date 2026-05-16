package Client.networking.endpoints;

import Client.model.Item;
import Client.networking.ApiClient;
import Client.networking.ApiResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class ItemApi {

    private final ApiClient apiClient = new ApiClient();
    private final Gson gson = new Gson();

    public ApiResponse<List<Item>> getMyItems() {
        try {
            String json = apiClient.get("/items");
            return gson.fromJson(json, new TypeToken<ApiResponse<List<Item>>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Item> getItem(int itemId) {
        try {
            String json = apiClient.post("/items/get", new ItemIdBody(itemId));
            return gson.fromJson(json, new TypeToken<ApiResponse<Item>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> createItem(String name, String description, String category, String condition) {
        try {
            String json = apiClient.post("/items/create", new CreateBody(name, description, category, condition));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> updateItem(int itemId, String name, String description, String category, String condition) {
        try {
            String json = apiClient.post("/items/update", new UpdateBody(itemId, name, description, category, condition));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> deleteItem(int itemId) {
        try {
            String json = apiClient.post("/items/delete", new ItemIdBody(itemId));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    private <T> ApiResponse<T> error(Exception e) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setStatus(500);
        r.setMessage(e.getMessage());
        return r;
    }

    private static class ItemIdBody {
        int itemId;
        ItemIdBody(int id) { this.itemId = id; }
    }

    private static class CreateBody {
        String name, description, category, condition;
        CreateBody(String n, String d, String cat, String cond) {
            name = n; description = d; category = cat; condition = cond;
        }
    }

    private static class UpdateBody {
        int itemId;
        String name, description, category, condition;
        UpdateBody(int id, String n, String d, String cat, String cond) {
            itemId = id; name = n; description = d; category = cat; condition = cond;
        }
    }
}
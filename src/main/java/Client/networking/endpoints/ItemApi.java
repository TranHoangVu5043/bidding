package Client.networking.endpoints;

import Client.dto.requests.CreateItemBody;
import Client.dto.requests.ItemIdBody;
import Client.dto.requests.UpdateItemBody;
import Client.model.item.Item;
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
    public ApiResponse<List<Item>> getAllItems() {
        try {
            String json = apiClient.get("/items/all");
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

    public ApiResponse<Void> createItem(String name, String description, String category, String condition, double price, int stock) {
        try {
            String json = apiClient.post("/items/create", new CreateItemBody(name, description, category, condition, price, stock));
            return gson.fromJson(json, new TypeToken<ApiResponse<Void>>() {}.getType());
        } catch (Exception e) {
            return error(e);
        }
    }

    public ApiResponse<Void> updateItem(int itemId, String name, String description, String category, String condition) {
        try {
            String json = apiClient.post("/items/update", new UpdateItemBody(itemId, name, description, category, condition));
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

}
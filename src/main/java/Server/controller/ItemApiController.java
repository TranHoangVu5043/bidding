package Server.controller;

import Server.controller.responseObjects.ApiResponse;
import Server.model.auction.items.Item;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.ItemService;

import com.google.gson.Gson;

import java.util.List;

public class ItemApiController {

    private final ItemService itemService;
    private final Gson gson;

    public ItemApiController(ItemService itemService) {
        this.itemService = itemService;
        this.gson = new Gson();
    }

    // GET /api/items
    // Returns all items owned by the logged-in seller
    public void getMyItems(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            List<Item> items = itemService.getItemsByOwner(user.getId());
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", items)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/items/get
    // Body: { "itemId": 1 }
    public void getItem(RequestWrapper req, ResponseWrapper res) {
        try {
            ItemIdRequest body = gson.fromJson(req.getBody(), ItemIdRequest.class);
            if (body == null || body.itemId <= 0) {
                res.error(400, "Missing required field: itemId");
                return;
            }

            Item item = itemService.getItemDetail(body.itemId);
            if (item == null) {
                res.error(404, "Item not found");
                return;
            }

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", item)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/items/create
    // Body: { "name": "...", "description": "...", "category": "ELECTRONICS", "condition": "NEW" }
    public void createItem(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }
            if (!"seller".equalsIgnoreCase(user.getRole())) {
                res.error(403, "Only sellers can add items");
                return;
            }

            CreateItemRequest body = gson.fromJson(req.getBody(), CreateItemRequest.class);
            if (body == null || body.name == null || body.name.isBlank()
                    || body.category == null || body.condition == null) {
                res.error(400, "Missing required fields: name, category, condition");
                return;
            }

            Item item = new Item(0, body.name, body.description, user.getId(), body.category, body.condition) {};

            boolean added = itemService.addItem(item);
            if (!added) {
                res.error(400, "Failed to add item");
                return;
            }

            res.sendJson(201, gson.toJson(new ApiResponse<>(201, "Item created", null)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/items/update
    // Body: { "itemId": 1, "name": "...", "description": "...", "category": "...", "condition": "..." }
    public void updateItem(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            UpdateItemRequest body = gson.fromJson(req.getBody(), UpdateItemRequest.class);
            if (body == null || body.itemId <= 0) {
                res.error(400, "Missing required field: itemId");
                return;
            }

            if (!itemService.canModifyItem(body.itemId, user.getId())) {
                res.error(403, "Item not found or not owned by you");
                return;
            }

            Item existing = itemService.getItemDetail(body.itemId);
            if (body.name != null) existing.setName(body.name);
            if (body.description != null) existing.setDescription(body.description);
            if (body.category != null) existing.setCategory(body.category);
            if (body.condition != null) existing.setCondition(body.condition);

            boolean updated = itemService.updateItem(existing);
            if (!updated) {
                res.error(400, "Failed to update item");
                return;
            }

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Item updated", null)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // POST /api/items/delete
    // Body: { "itemId": 1 }
    public void deleteItem(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            ItemIdRequest body = gson.fromJson(req.getBody(), ItemIdRequest.class);
            if (body == null || body.itemId <= 0) {
                res.error(400, "Missing required field: itemId");
                return;
            }

            if (!itemService.canModifyItem(body.itemId, user.getId())) {
                res.error(403, "Item not found or not owned by you");
                return;
            }

            boolean deleted = itemService.deleteItem(body.itemId);
            if (!deleted) {
                res.error(400, "Failed to delete item");
                return;
            }

            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "Item deleted", null)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    private static class ItemIdRequest {
        int itemId;
    }

    private static class CreateItemRequest {
        String name;
        String description;
        String category;
        String condition;
    }

    private static class UpdateItemRequest {
        int itemId;
        String name;
        String description;
        String category;
        String condition;
    }
}

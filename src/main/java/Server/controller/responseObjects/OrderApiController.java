package Server.controller.responseObjects;

import Server.model.auction.items.Order;
import Server.model.users.User;
import Server.networking.http.RequestWrapper;
import Server.networking.http.ResponseWrapper;
import Server.service.auction.OrderService;
import com.google.gson.Gson;

import java.util.List;

public class OrderApiController {

    private final OrderService orderService;
    private final Gson gson;

    public OrderApiController(OrderService orderService) {
        this.orderService = orderService;
        this.gson = new Gson();
    }

    // GET /orders/recent
    public void getRecentOrders(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            List<Order> orders = orderService.getOrdersBySeller(user.getId());
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", orders)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }

    // GET /orders/all
    public void getAllOrders(RequestWrapper req, ResponseWrapper res) {
        try {
            User user = req.getUser();
            if (user == null) { res.error(401, "Unauthorized"); return; }

            List<Order> orders = orderService.getOrdersBySeller(user.getId());
            res.sendJson(200, gson.toJson(new ApiResponse<>(200, "OK", orders)));
        } catch (Exception e) {
            res.error(500, "Server error: " + e.getMessage());
        }
    }
}

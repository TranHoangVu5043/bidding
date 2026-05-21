package Server.service.auction;

import Server.dao.auction.OrderDAO;
import Server.model.auction.items.Order;

import java.util.List;

public class OrderService {
    private final OrderDAO orderDAO;

    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    public List<Order> getOrdersBySeller(int sellerId) {
        return orderDAO.findBySellerId(sellerId);
    }

    public boolean createOrder(Order order) {
        if (order.getProductName() == null || order.getProductName().isBlank()) return false;
        if (order.getTotalAmount() < 0) return false;
        orderDAO.create(order);
        return true;
    }
}

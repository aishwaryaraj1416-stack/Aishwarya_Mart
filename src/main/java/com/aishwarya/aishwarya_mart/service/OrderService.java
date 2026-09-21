package com.aishwarya.aishwarya_mart.service;

import com.aishwarya.aishwarya_mart.dao.CartDAO;
import com.aishwarya.aishwarya_mart.dao.OrderDAO;
import com.aishwarya.aishwarya_mart.dao.ProductDAO;
import com.aishwarya.aishwarya_mart.model.CartItem;
import com.aishwarya.aishwarya_mart.model.Order;
import com.aishwarya.aishwarya_mart.model.OrderItem;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    private final CartDAO cartDAO;
    private final OrderDAO orderDAO;
    private final ProductDAO productDAO;

    public OrderService() {
        this.cartDAO = new CartDAO();
        this.orderDAO = new OrderDAO();
        this.productDAO = new ProductDAO();
    }

    public int checkout(int userId) {

        List<CartItem> cartItems =
                cartDAO.getCartItems(userId);

        if (cartItems.isEmpty()) {
            return -1;
        }

        BigDecimal total = BigDecimal.ZERO;

        /*
         * Check stock before creating the order.
         */
        for (CartItem item : cartItems) {

            var product =
                    productDAO.getProductById(
                            item.getProductId()
                    );

            if (product == null) {
                return -1;
            }

            if (product.getStockQty()
                    < item.getQuantity()) {

                return -1;
            }

            BigDecimal itemTotal =
                    item.getPrice().multiply(
                            BigDecimal.valueOf(
                                    item.getQuantity()
                            )
                    );

            total = total.add(itemTotal);
        }

        /*
         * Create the order.
         */
        Order order = new Order(
                userId,
                total,
                "PENDING"
        );

        int orderId =
                orderDAO.createOrder(order);

        if (orderId == -1) {
            return -1;
        }

        /*
         * Add each product to the order and
         * reduce its stock.
         */
        for (CartItem item : cartItems) {

            boolean stockReduced =
                    productDAO.reduceStock(
                            item.getProductId(),
                            item.getQuantity()
                    );

            if (!stockReduced) {
                return -1;
            }

            OrderItem orderItem =
                    new OrderItem(
                            orderId,
                            item.getProductId(),
                            item.getQuantity(),
                            item.getPrice()
                    );

            if (!orderDAO.addOrderItem(orderItem)) {
                return -1;
            }
        }

        /*
         * Clear the cart only after all
         * order items were created.
         */
        boolean cartCleared =
                cartDAO.clearCart(userId);

        if (!cartCleared) {
            return -1;
        }

        return orderId;
    }
}
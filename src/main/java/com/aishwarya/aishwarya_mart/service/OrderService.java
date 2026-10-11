package com.aishwarya.aishwarya_mart.service;

import com.aishwarya.aishwarya_mart.util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    /**
     * Places an order as one database transaction. An order, its items,
     * stock deductions and cart cleanup either all succeed or all roll back.
     */
    public int checkout(int userId, String deliveryName, String deliveryEmail,
                        String deliveryPhone, String deliveryAddress,
                        String deliveryCity, String deliveryPincode,
                        String paymentMethod) {
        if (userId <= 0) {
            return -1;
        }

        try (Connection connection = DBUtil.getConnection()) {
            connection.setAutoCommit(false);

            try {
                List<CheckoutLine> lines = loadAndLockCart(connection, userId);
                if (lines.isEmpty()) {
                    connection.rollback();
                    return -1;
                }

                BigDecimal total = BigDecimal.ZERO;
                for (CheckoutLine line : lines) {
                    if (line.quantity() <= 0 || line.price() == null
                            || line.price().signum() < 0
                            || line.stockQty() < line.quantity()) {
                        connection.rollback();
                        return -1;
                    }
                    total = total.add(line.price().multiply(
                            BigDecimal.valueOf(line.quantity())));
                }

                int orderId = insertOrder(connection, userId, total, deliveryName,
                        deliveryEmail, deliveryPhone, deliveryAddress,
                        deliveryCity, deliveryPincode, paymentMethod);
                if (orderId <= 0) {
                    connection.rollback();
                    return -1;
                }

                for (CheckoutLine line : lines) {
                    if (!reduceStock(connection, line.productId(), line.quantity())) {
                        connection.rollback();
                        return -1;
                    }
                    insertOrderItem(connection, orderId, line);
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM cart_items WHERE user_id = ?")) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                }

                connection.commit();
                return orderId;
            } catch (Exception e) {
                try {
                    connection.rollback();
                } catch (Exception rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                e.printStackTrace();
                return -1;
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (Exception ignored) {
                    // The pooled connection will be returned by try-with-resources.
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    private List<CheckoutLine> loadAndLockCart(Connection connection, int userId)
            throws Exception {
        String sql = """
                SELECT c.product_id, c.quantity, p.price, p.stock_qty
                FROM cart_items c
                INNER JOIN products p ON p.id = c.product_id
                WHERE c.user_id = ?
                ORDER BY c.product_id
                FOR UPDATE
                """;

        List<CheckoutLine> lines = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    lines.add(new CheckoutLine(
                            result.getInt("product_id"),
                            result.getInt("quantity"),
                            result.getBigDecimal("price"),
                            result.getInt("stock_qty")
                    ));
                }
            }
        }
        return lines;
    }

    private int insertOrder(Connection connection, int userId, BigDecimal total,
                            String deliveryName, String deliveryEmail,
                            String deliveryPhone, String deliveryAddress,
                            String deliveryCity, String deliveryPincode,
                            String paymentMethod) throws Exception {
        String sql = """
                INSERT INTO orders
                    (user_id, total_amount, status, delivery_name, delivery_email,
                     delivery_phone, delivery_address, delivery_city,
                     delivery_pincode, payment_method)
                VALUES (?, ?, 'PENDING', ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setBigDecimal(2, total);
            statement.setString(3, deliveryName);
            statement.setString(4, deliveryEmail);
            statement.setString(5, deliveryPhone);
            statement.setString(6, deliveryAddress);
            statement.setString(7, deliveryCity);
            statement.setString(8, deliveryPincode);
            statement.setString(9, paymentMethod);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    private boolean reduceStock(Connection connection, int productId, int quantity)
            throws Exception {
        String sql = """
                UPDATE products
                SET stock_qty = stock_qty - ?
                WHERE id = ? AND stock_qty >= ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);
            return statement.executeUpdate() == 1;
        }
    }

    private void insertOrderItem(Connection connection, int orderId, CheckoutLine line)
            throws Exception {
        String sql = """
                INSERT INTO order_items (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, line.productId());
            statement.setInt(3, line.quantity());
            statement.setBigDecimal(4, line.price());
            statement.executeUpdate();
        }
    }

    private record CheckoutLine(
            int productId,
            int quantity,
            BigDecimal price,
            int stockQty) {
    }
}

package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.model.Order;
import com.aishwarya.aishwarya_mart.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO extends BaseDAO {

    public int createOrder(Order order) {

        String sql = """
                INSERT INTO orders (user_id, total_amount, status)
                VALUES (?, ?, ?)
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        sql,
                        java.sql.Statement.RETURN_GENERATED_KEYS
                )
        ) {

            statement.setInt(1, order.getUserId());
            statement.setBigDecimal(2, order.getTotalAmount());
            statement.setString(3, order.getStatus());

            statement.executeUpdate();

            try (ResultSet result = statement.getGeneratedKeys()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public boolean addOrderItem(OrderItem item) {

        String sql = """
                INSERT INTO order_items
                (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, item.getOrderId());
            statement.setInt(2, item.getProductId());
            statement.setInt(3, item.getQuantity());
            statement.setBigDecimal(4, item.getPrice());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getOrdersByUser(int userId) {

        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT id, user_id, total_amount, status, created_at
                FROM orders
                WHERE user_id = ?
                ORDER BY created_at DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    orders.add(mapOrder(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<Order> getOrdersBySeller(int sellerId) {

        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT DISTINCT
                    o.id,
                    o.user_id,
                    o.total_amount,
                    o.status,
                    o.created_at
                FROM orders o
                INNER JOIN order_items oi
                    ON o.id = oi.order_id
                INNER JOIN products p
                    ON oi.product_id = p.id
                WHERE p.seller_id = ?
                ORDER BY o.created_at DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    orders.add(mapOrder(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    public List<OrderItem> getOrderItems(int orderId) {

        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT
                    oi.id,
                    oi.order_id,
                    oi.product_id,
                    oi.quantity,
                    oi.price,
                    p.name AS product_name,
                    p.image_url
                FROM order_items oi
                LEFT JOIN products p
                    ON oi.product_id = p.id
                WHERE oi.order_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, orderId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    items.add(mapOrderItem(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public List<OrderItem> getOrderItemsBySeller(
            int orderId,
            int sellerId) {

        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT
                    oi.id,
                    oi.order_id,
                    oi.product_id,
                    oi.quantity,
                    oi.price,
                    p.name AS product_name,
                    p.image_url
                FROM order_items oi
                INNER JOIN products p
                    ON oi.product_id = p.id
                WHERE oi.order_id = ?
                  AND p.seller_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, orderId);
            statement.setInt(2, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    items.add(mapOrderItem(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public boolean sellerOwnsOrder(
            int orderId,
            int sellerId) {

        String sql = """
                SELECT COUNT(*)
                FROM order_items oi
                INNER JOIN products p
                    ON oi.product_id = p.id
                WHERE oi.order_id = ?
                  AND p.seller_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, orderId);
            statement.setInt(2, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int getSellerOrderCount(int sellerId) {

        String sql = """
                SELECT COUNT(DISTINCT o.id)
                FROM orders o
                INNER JOIN order_items oi
                    ON o.id = oi.order_id
                INNER JOIN products p
                    ON oi.product_id = p.id
                WHERE p.seller_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public java.math.BigDecimal getSellerRevenue(int sellerId) {

        String sql = """
                SELECT COALESCE(
                    SUM(oi.quantity * oi.price),
                    0
                )
                FROM order_items oi
                INNER JOIN products p
                    ON oi.product_id = p.id
                INNER JOIN orders o
                    ON oi.order_id = o.id
                WHERE p.seller_id = ?
                  AND o.status <> 'CANCELLED'
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getBigDecimal(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return java.math.BigDecimal.ZERO;
    }

    public boolean updateOrderStatus(
            int orderId,
            String status) {

        String sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, status);
            statement.setInt(2, orderId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateOrderStatusBySeller(
            int orderId,
            int sellerId,
            String status) {

        String sql = """
                UPDATE orders
                SET status = ?
                WHERE id = ?
                  AND EXISTS (
                      SELECT 1
                      FROM order_items oi
                      INNER JOIN products p
                          ON oi.product_id = p.id
                      WHERE oi.order_id = orders.id
                        AND p.seller_id = ?
                  )
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, status);
            statement.setInt(2, orderId);
            statement.setInt(3, sellerId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Order> getAllOrders() {

        List<Order> orders = new ArrayList<>();

        String sql = """
                SELECT id, user_id, total_amount, status, created_at
                FROM orders
                ORDER BY created_at DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result =
                        statement.executeQuery()
        ) {

            while (result.next()) {
                orders.add(mapOrder(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return orders;
    }

    private Order mapOrder(ResultSet result)
            throws Exception {

        return new Order(
                result.getInt("id"),
                result.getInt("user_id"),
                result.getBigDecimal("total_amount"),
                result.getString("status"),
                result.getTimestamp("created_at")
                        .toLocalDateTime()
        );
    }

    private OrderItem mapOrderItem(ResultSet result)
            throws Exception {

        OrderItem item = new OrderItem(
                result.getInt("id"),
                result.getInt("order_id"),
                result.getInt("product_id"),
                result.getInt("quantity"),
                result.getBigDecimal("price")
        );

        item.setProductName(
                result.getString("product_name")
        );

        item.setImageUrl(
                result.getString("image_url")
        );

        return item;
    }
}

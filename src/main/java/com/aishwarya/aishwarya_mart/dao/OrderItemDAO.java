package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderItemDAO extends BaseDAO {

    public boolean addOrderItem(OrderItem item) {

        String sql = """
                INSERT INTO order_items
                (order_id, product_id, quantity, price)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
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

    public List<OrderItem> getItemsByOrderId(int orderId) {

        List<OrderItem> items = new ArrayList<>();

        String sql = """
                SELECT id, order_id, product_id, quantity, price
                FROM order_items
                WHERE order_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {

            statement.setInt(1, orderId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    items.add(new OrderItem(
                            result.getInt("id"),
                            result.getInt("order_id"),
                            result.getInt("product_id"),
                            result.getInt("quantity"),
                            result.getBigDecimal("price")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }
}

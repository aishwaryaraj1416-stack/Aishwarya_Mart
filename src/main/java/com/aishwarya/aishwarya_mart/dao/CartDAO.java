package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.model.CartItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CartDAO extends BaseDAO {

    public boolean addToCart(
            int userId,
            int productId,
            int quantity) {

        if (quantity <= 0) {
            return false;
        }

        String checkSql = """
                SELECT quantity
                FROM cart_items
                WHERE user_id = ?
                  AND product_id = ?
                """;

        String insertSql = """
                INSERT INTO cart_items
                    (user_id, product_id, quantity)
                VALUES (?, ?, ?)
                """;

        String updateSql = """
                UPDATE cart_items
                SET quantity = quantity + ?
                WHERE user_id = ?
                  AND product_id = ?
                """;

        try (Connection connection = getConnection()) {

            try (PreparedStatement checkStatement =
                         connection.prepareStatement(checkSql)) {

                checkStatement.setInt(1, userId);
                checkStatement.setInt(2, productId);

                try (ResultSet result =
                             checkStatement.executeQuery()) {

                    if (result.next()) {

                        try (PreparedStatement updateStatement =
                                     connection.prepareStatement(updateSql)) {

                            updateStatement.setInt(1, quantity);
                            updateStatement.setInt(2, userId);
                            updateStatement.setInt(3, productId);

                            return updateStatement.executeUpdate() > 0;
                        }

                    } else {

                        try (PreparedStatement insertStatement =
                                     connection.prepareStatement(insertSql)) {

                            insertStatement.setInt(1, userId);
                            insertStatement.setInt(2, productId);
                            insertStatement.setInt(3, quantity);

                            return insertStatement.executeUpdate() > 0;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<CartItem> getCartItems(int userId) {

        List<CartItem> items = new ArrayList<>();

        String sql = """
                SELECT c.id,
                       c.user_id,
                       c.product_id,
                       c.quantity,
                       p.name,
                       p.price,
                       p.image_url
                FROM cart_items c
                JOIN products p ON c.product_id = p.id
                WHERE c.user_id = ?
                ORDER BY c.id DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {

                    CartItem item = new CartItem(
                            result.getInt("id"),
                            result.getInt("user_id"),
                            result.getInt("product_id"),
                            result.getInt("quantity")
                    );

                    item.setProductName(
                            result.getString("name")
                    );

                    item.setPrice(
                            result.getBigDecimal("price")
                    );

                    item.setImageUrl(
                            result.getString("image_url")
                    );

                    items.add(item);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    public boolean updateQuantity(
            int userId,
            int productId,
            int quantity) {

        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }

        String sql = """
                UPDATE cart_items
                SET quantity = ?
                WHERE user_id = ?
                  AND product_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, quantity);
            statement.setInt(2, userId);
            statement.setInt(3, productId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeFromCart(
            int userId,
            int productId) {

        String sql = """
                DELETE FROM cart_items
                WHERE user_id = ?
                  AND product_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);
            statement.setInt(2, productId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean clearCart(int userId) {

        String sql = """
                DELETE FROM cart_items
                WHERE user_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            statement.executeUpdate();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
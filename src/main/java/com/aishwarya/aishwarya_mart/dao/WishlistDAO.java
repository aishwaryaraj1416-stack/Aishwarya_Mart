package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class WishlistDAO extends BaseDAO {

    public boolean addToWishlist(int userId, int productId) {

        String sql = """
                INSERT INTO wishlist (user_id, product_id)
                VALUES (?, ?)
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

    public boolean removeFromWishlist(
            int userId,
            int productId) {

        String sql = """
                DELETE FROM wishlist
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

    public boolean isInWishlist(
            int userId,
            int productId) {

        String sql = """
                SELECT id
                FROM wishlist
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

            try (ResultSet result =
                         statement.executeQuery()) {

                return result.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Product> getWishlistProducts(int userId) {

        List<Product> products = new ArrayList<>();

        String sql = """
                SELECT p.id,
                       p.seller_id,
                       p.name,
                       p.description,
                       p.price,
                       p.stock_qty,
                       p.category,
                       p.image_url
                FROM wishlist w
                JOIN products p
                  ON w.product_id = p.id
                WHERE w.user_id = ?
                ORDER BY w.created_at DESC
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

                    products.add(
                            new Product(
                                    result.getInt("id"),
                                    result.getInt("seller_id"),
                                    result.getString("name"),
                                    result.getString("description"),
                                    result.getBigDecimal("price"),
                                    result.getInt("stock_qty"),
                                    result.getString("category"),
                                    result.getString("image_url")
                            )
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public boolean clearWishlist(int userId) {

        String sql = """
                DELETE FROM wishlist
                WHERE user_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, userId);

            return statement.executeUpdate() >= 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

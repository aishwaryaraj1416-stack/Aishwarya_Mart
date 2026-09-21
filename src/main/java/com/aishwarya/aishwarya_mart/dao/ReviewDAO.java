package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO extends BaseDAO {

    public boolean addReview(Review review) {

        String sql = """
                INSERT INTO reviews
                (product_id, user_id, rating, comment)
                VALUES (?, ?, ?, ?)
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, review.getProductId());
            statement.setInt(2, review.getUserId());
            statement.setInt(3, review.getRating());
            statement.setString(4, review.getComment());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Review> getReviewsByProduct(int productId) {

        List<Review> reviews = new ArrayList<>();

        String sql = """
                SELECT id, product_id, user_id,
                       rating, comment, created_at
                FROM reviews
                WHERE product_id = ?
                ORDER BY created_at DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, productId);

            try (ResultSet result =
                         statement.executeQuery()) {

                while (result.next()) {
                    reviews.add(mapReview(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return reviews;
    }

    public double getAverageRating(int productId) {

        String sql = """
                SELECT COALESCE(AVG(rating), 0)
                FROM reviews
                WHERE product_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, productId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (result.next()) {
                    return result.getDouble(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    public int getReviewCount(int productId) {

        String sql = """
                SELECT COUNT(*)
                FROM reviews
                WHERE product_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, productId);

            try (ResultSet result =
                         statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt(1);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public boolean hasReviewed(
            int userId,
            int productId) {

        String sql = """
                SELECT COUNT(*)
                FROM reviews
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

                if (result.next()) {
                    return result.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public boolean hasDeliveredProduct(
            int userId,
            int productId) {

        String sql = """
                SELECT COUNT(*)
                FROM orders o
                JOIN order_items oi
                    ON o.id = oi.order_id
                WHERE o.user_id = ?
                  AND oi.product_id = ?
                  AND UPPER(o.status) = 'DELIVERED'
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

                if (result.next()) {
                    return result.getInt(1) > 0;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private Review mapReview(
            ResultSet result) throws Exception {

        Review review = new Review();

        review.setId(result.getInt("id"));
        review.setProductId(
                result.getInt("product_id"));
        review.setUserId(
                result.getInt("user_id"));
        review.setRating(
                result.getInt("rating"));
        review.setComment(
                result.getString("comment"));

        if (result.getTimestamp("created_at") != null) {
            review.setCreatedAt(
                    result.getTimestamp("created_at")
                            .toLocalDateTime()
            );
        }

        return review;
    }
}

package com.aishwarya.aishwarya_mart.dao;

import com.aishwarya.aishwarya_mart.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO extends BaseDAO {

    private static final String PRODUCT_SELECT = """
            SELECT p.id, p.seller_id, p.name, p.description,
                   p.price, p.stock_qty, p.category, p.image_url,
                   COALESCE(AVG(r.rating), 0) AS rating,
                   COUNT(r.id) AS review_count
            FROM products p
            LEFT JOIN reviews r
                ON p.id = r.product_id
            """;

    public List<Product> getAllProducts() {

        List<Product> products = new ArrayList<>();

        String sql = PRODUCT_SELECT + """
                GROUP BY p.id, p.seller_id, p.name, p.description,
                         p.price, p.stock_qty, p.category, p.image_url
                ORDER BY p.id DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {

            while (result.next()) {
                products.add(mapProductWithRating(result));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> getProductsBySeller(int sellerId) {

        List<Product> products = new ArrayList<>();

        String sql = PRODUCT_SELECT + """
                WHERE p.seller_id = ?
                GROUP BY p.id, p.seller_id, p.name, p.description,
                         p.price, p.stock_qty, p.category, p.image_url
                ORDER BY p.id DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, sellerId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProductWithRating(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public Product getProductById(int id) {

        String sql = PRODUCT_SELECT + """
                WHERE p.id = ?
                GROUP BY p.id, p.seller_id, p.name, p.description,
                         p.price, p.stock_qty, p.category, p.image_url
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapProductWithRating(result);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Product> getProductsByCategory(String category) {

        List<Product> products = new ArrayList<>();

        String sql = PRODUCT_SELECT + """
                WHERE LOWER(p.category) = LOWER(?)
                GROUP BY p.id, p.seller_id, p.name, p.description,
                         p.price, p.stock_qty, p.category, p.image_url
                ORDER BY p.id DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, category);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProductWithRating(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public List<Product> searchProducts(String keyword) {

        List<Product> products = new ArrayList<>();

        String sql = PRODUCT_SELECT + """
                WHERE LOWER(p.name) LIKE ?
                   OR LOWER(p.description) LIKE ?
                   OR LOWER(p.category) LIKE ?
                GROUP BY p.id, p.seller_id, p.name, p.description,
                         p.price, p.stock_qty, p.category, p.image_url
                ORDER BY p.id DESC
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            String searchPattern =
                    "%" + keyword.trim().toLowerCase() + "%";

            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    products.add(mapProductWithRating(result));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return products;
    }

    public boolean addProduct(Product product) {

        String sql = """
                INSERT INTO products
                (seller_id, name, description, price,
                 stock_qty, category, image_url)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, product.getSellerId());
            statement.setString(2, product.getName());
            statement.setString(3, product.getDescription());
            statement.setBigDecimal(4, product.getPrice());
            statement.setInt(5, product.getStockQty());
            statement.setString(6, product.getCategory());
            statement.setString(7, product.getImageUrl());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product product) {

        String sql = """
                UPDATE products
                SET name = ?,
                    description = ?,
                    price = ?,
                    stock_qty = ?,
                    category = ?,
                    image_url = ?
                WHERE id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQty());
            statement.setString(5, product.getCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getId());

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProductBySeller(Product product, int sellerId) {

        String sql = """
                UPDATE products
                SET name = ?,
                    description = ?,
                    price = ?,
                    stock_qty = ?,
                    category = ?,
                    image_url = ?
                WHERE id = ?
                  AND seller_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, product.getName());
            statement.setString(2, product.getDescription());
            statement.setBigDecimal(3, product.getPrice());
            statement.setInt(4, product.getStockQty());
            statement.setString(5, product.getCategory());
            statement.setString(6, product.getImageUrl());
            statement.setInt(7, product.getId());
            statement.setInt(8, sellerId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int id) {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProductBySeller(int id, int sellerId) {

        String sql = """
                DELETE FROM products
                WHERE id = ?
                  AND seller_id = ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, id);
            statement.setInt(2, sellerId);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean reduceStock(int productId, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        String sql = """
                UPDATE products
                SET stock_qty = stock_qty - ?
                WHERE id = ?
                  AND stock_qty >= ?
                """;

        try (
                Connection connection = getConnection();
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(1, quantity);
            statement.setInt(2, productId);
            statement.setInt(3, quantity);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private Product mapProductWithRating(ResultSet result)
            throws Exception {

        Product product = new Product(
                result.getInt("id"),
                result.getInt("seller_id"),
                result.getString("name"),
                result.getString("description"),
                result.getBigDecimal("price"),
                result.getInt("stock_qty"),
                result.getString("category"),
                result.getString("image_url")
        );

        product.setRating(result.getDouble("rating"));
        product.setReviewCount(result.getInt("review_count"));

        return product;
    }
}

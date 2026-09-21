package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.ProductDAO;
import com.aishwarya.aishwarya_mart.model.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/products")
public class ProductServlet extends HttpServlet {

    private ProductDAO productDAO;

    @Override
    public void init() {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("get".equals(action)) {

            getProduct(request, response);

        } else if ("category".equals(action)) {

            getByCategory(request, response);

        } else if ("search".equals(action)) {

            searchProducts(request, response);

        } else if ("mine".equals(action)) {

            getMyProducts(request, response);

        } else {

            getAllProducts(request, response);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("add".equals(action)) {

            addProduct(request, response);

        } else if ("update".equals(action)) {

            updateProduct(request, response);

        } else if ("delete".equals(action)) {

            deleteProduct(request, response);

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action"
            );
        }
    }

    private void getAllProducts(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        List<Product> products =
                productDAO.getAllProducts();

        sendProducts(response, products);
    }

    private void getMyProducts(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (!isSellerOrAdmin(session)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Seller or admin access required"
            );
            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String role =
                (String) session.getAttribute("role");

        List<Product> products;

        if ("ADMIN".equals(role)) {

            products =
                    productDAO.getAllProducts();

        } else {

            products =
                    productDAO.getProductsBySeller(userId);
        }

        sendProducts(response, products);
    }

    private void sendProducts(
            HttpServletResponse response,
            List<Product> products)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        com.google.gson.Gson gson =
                new com.google.gson.Gson();

        response.getWriter().write(
                gson.toJson(products)
        );
    }

    private void getProduct(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idParameter =
                request.getParameter("id");

        if (idParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID is required"
            );
            return;
        }

        try {

            int id =
                    Integer.parseInt(idParameter);

            Product product =
                    productDAO.getProductById(id);

            if (product == null) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found"
                );
                return;
            }

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            com.google.gson.Gson gson =
                    new com.google.gson.Gson();

            response.getWriter().write(
                    gson.toJson(product)
            );

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID"
            );
        }
    }

    private void getByCategory(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String category =
                request.getParameter("category");

        if (category == null ||
                category.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category is required"
            );
            return;
        }

        List<Product> products =
                productDAO.getProductsByCategory(category);

        sendProducts(response, products);
    }

    private void searchProducts(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String keyword =
                request.getParameter("keyword");

        if (keyword == null ||
                keyword.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Keyword is required"
            );
            return;
        }

        List<Product> products =
                productDAO.searchProducts(keyword);

        sendProducts(response, products);
    }

    private boolean isSellerOrAdmin(
            HttpSession session) {

        if (session == null ||
                session.getAttribute("userId") == null) {

            return false;
        }

        String role =
                (String) session.getAttribute("role");

        return "SELLER".equals(role) ||
                "ADMIN".equals(role);
    }

    private void addProduct(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (!isSellerOrAdmin(session)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Seller or admin access required"
            );
            return;
        }

        String name =
                request.getParameter("name");

        String description =
                request.getParameter("description");

        String priceParameter =
                request.getParameter("price");

        String stockParameter =
                request.getParameter("stockQty");

        String category =
                request.getParameter("category");

        String imageUrl =
                request.getParameter("imageUrl");

        if (name == null ||
                name.isBlank() ||
                category == null ||
                category.isBlank() ||
                priceParameter == null ||
                stockParameter == null) {

            response.sendRedirect(
                    "seller.html?error=invalid"
            );
            return;
        }

        try {

            BigDecimal price =
                    new BigDecimal(priceParameter);

            int stockQty =
                    Integer.parseInt(stockParameter);

            if (price.compareTo(BigDecimal.ZERO) < 0 ||
                    stockQty < 0) {

                response.sendRedirect(
                        "seller.html?error=invalid"
                );
                return;
            }

            int sellerId =
                    (Integer) session.getAttribute("userId");

            Product product =
                    new Product(
                            sellerId,
                            name.trim(),
                            description,
                            price,
                            stockQty,
                            category.trim(),
                            imageUrl
                    );

            boolean added =
                    productDAO.addProduct(product);

            if (added) {

                response.sendRedirect(
                        "seller.html?success=added"
                );

            } else {

                response.sendRedirect(
                        "seller.html?error=failed"
                );
            }

        } catch (Exception e) {

            response.sendRedirect(
                    "seller.html?error=invalid"
            );
        }
    }

    private void updateProduct(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (!isSellerOrAdmin(session)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Seller or admin access required"
            );
            return;
        }

        try {

            int id =
                    Integer.parseInt(
                            request.getParameter("id")
                    );

            Product existingProduct =
                    productDAO.getProductById(id);

            if (existingProduct == null) {

                response.sendRedirect(
                        "seller.html?error=failed"
                );
                return;
            }

            String role =
                    (String) session.getAttribute("role");

            int currentUserId =
                    (Integer) session.getAttribute("userId");

            if (!"ADMIN".equals(role) &&
                    existingProduct.getSellerId() != currentUserId) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You cannot edit this product"
                );
                return;
            }

            String name =
                    request.getParameter("name");

            String description =
                    request.getParameter("description");

            String priceParameter =
                    request.getParameter("price");

            String stockParameter =
                    request.getParameter("stockQty");

            String category =
                    request.getParameter("category");

            String imageUrl =
                    request.getParameter("imageUrl");

            if (name == null ||
                    name.isBlank() ||
                    category == null ||
                    category.isBlank() ||
                    priceParameter == null ||
                    stockParameter == null) {

                response.sendRedirect(
                        "seller.html?error=invalid"
                );
                return;
            }

            BigDecimal price =
                    new BigDecimal(priceParameter);

            int stockQty =
                    Integer.parseInt(stockParameter);

            if (price.compareTo(BigDecimal.ZERO) < 0 ||
                    stockQty < 0) {

                response.sendRedirect(
                        "seller.html?error=invalid"
                );
                return;
            }

            Product product =
                    new Product();

            product.setId(id);
            product.setName(name.trim());
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQty(stockQty);
            product.setCategory(category.trim());
            product.setImageUrl(imageUrl);

            boolean updated;

            if ("ADMIN".equals(role)) {

                updated =
                        productDAO.updateProduct(product);

            } else {

                updated =
                        productDAO.updateProductBySeller(
                                product,
                                currentUserId
                        );
            }

            if (updated) {

                response.sendRedirect(
                        "seller.html?success=updated"
                );

            } else {

                response.sendRedirect(
                        "seller.html?error=failed"
                );
            }

        } catch (Exception e) {

            response.sendRedirect(
                    "seller.html?error=invalid"
            );
        }
    }

    private void deleteProduct(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (!isSellerOrAdmin(session)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Seller or admin access required"
            );
            return;
        }

        try {

            int id =
                    Integer.parseInt(
                            request.getParameter("id")
                    );

            Product existingProduct =
                    productDAO.getProductById(id);

            if (existingProduct == null) {

                response.sendRedirect(
                        "seller.html?error=failed"
                );
                return;
            }

            String role =
                    (String) session.getAttribute("role");

            int currentUserId =
                    (Integer) session.getAttribute("userId");

            if (!"ADMIN".equals(role) &&
                    existingProduct.getSellerId() != currentUserId) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You cannot delete this product"
                );
                return;
            }

            boolean deleted;

            if ("ADMIN".equals(role)) {

                deleted =
                        productDAO.deleteProduct(id);

            } else {

                deleted =
                        productDAO.deleteProductBySeller(
                                id,
                                currentUserId
                        );
            }

            if (deleted) {

                response.sendRedirect(
                        "seller.html?success=deleted"
                );

            } else {

                response.sendRedirect(
                        "seller.html?error=failed"
                );
            }

        } catch (Exception e) {

            response.sendRedirect(
                    "seller.html?error=invalid"
            );
        }
    }
}
package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.CartDAO;
import com.aishwarya.aishwarya_mart.dao.ProductDAO;
import com.aishwarya.aishwarya_mart.model.CartItem;
import com.aishwarya.aishwarya_mart.model.Product;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {

    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private Gson gson;

    @Override
    public void init() {
        cartDAO = new CartDAO();
        productDAO = new ProductDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please login first"
            );
            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        List<CartItem> items =
                cartDAO.getCartItems(userId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                gson.toJson(items)
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Please login first"
            );
            return;
        }

        int userId =
                (Integer) session.getAttribute("userId");

        String action =
                request.getParameter("action");

        try {

            if ("add".equals(action)) {

                addToCart(request, response, userId);

            } else if ("update".equals(action)) {

                updateCart(request, response, userId);

            } else if ("remove".equals(action)) {

                removeFromCart(request, response, userId);

            } else if ("clear".equals(action)) {

                boolean success =
                        cartDAO.clearCart(userId);

                sendJsonResponse(response, success);

            } else {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid cart action"
                );
            }

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid number"
            );
        }
    }

    private void addToCart(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId)
            throws IOException {

        String productIdParameter =
                request.getParameter("productId");

        String quantityParameter =
                request.getParameter("quantity");

        if (productIdParameter == null ||
                quantityParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID and quantity are required"
            );
            return;
        }

        int productId =
                Integer.parseInt(productIdParameter);

        int quantity =
                Integer.parseInt(quantityParameter);

        if (productId <= 0 ||
                quantity <= 0 ||
                quantity > 100) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid quantity or product ID"
            );
            return;
        }

        Product product =
                productDAO.getProductById(productId);

        if (product == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Product not found"
            );
            return;
        }

        if (product.getStockQty() < quantity) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Not enough stock available"
            );
            return;
        }

        boolean success =
                cartDAO.addToCart(
                        userId,
                        productId,
                        quantity
                );

        sendJsonResponse(response, success);
    }

    private void updateCart(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId)
            throws IOException {

        String productIdParameter =
                request.getParameter("productId");

        String quantityParameter =
                request.getParameter("quantity");

        if (productIdParameter == null ||
                quantityParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID and quantity are required"
            );
            return;
        }

        int productId =
                Integer.parseInt(productIdParameter);

        int quantity =
                Integer.parseInt(quantityParameter);

        if (productId <= 0) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID"
            );
            return;
        }

        if (quantity <= 0) {

            boolean removed =
                    cartDAO.removeFromCart(
                            userId,
                            productId
                    );

            sendJsonResponse(response, removed);
            return;
        }

        if (quantity > 100) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Maximum quantity is 100"
            );
            return;
        }

        Product product =
                productDAO.getProductById(productId);

        if (product == null) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Product not found"
            );
            return;
        }

        if (product.getStockQty() < quantity) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Not enough stock available"
            );
            return;
        }

        boolean success =
                cartDAO.updateQuantity(
                        userId,
                        productId,
                        quantity
                );

        sendJsonResponse(response, success);
    }

    private void removeFromCart(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId)
            throws IOException {

        String productIdParameter =
                request.getParameter("productId");

        if (productIdParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID is required"
            );
            return;
        }

        int productId =
                Integer.parseInt(productIdParameter);

        if (productId <= 0) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID"
            );
            return;
        }

        boolean success =
                cartDAO.removeFromCart(
                        userId,
                        productId
                );

        sendJsonResponse(response, success);
    }

    private void sendJsonResponse(
            HttpServletResponse response,
            boolean success)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                "{\"success\":" +
                        success +
                        "}"
        );
    }
}
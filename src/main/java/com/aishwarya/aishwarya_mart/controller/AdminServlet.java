package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.OrderDAO;
import com.aishwarya.aishwarya_mart.dao.ProductDAO;
import com.aishwarya.aishwarya_mart.dao.UserDAO;
import com.aishwarya.aishwarya_mart.model.Order;
import com.aishwarya.aishwarya_mart.model.OrderItem;
import com.aishwarya.aishwarya_mart.model.Product;
import com.aishwarya.aishwarya_mart.model.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {

    private UserDAO userDAO;
    private OrderDAO orderDAO;
    private ProductDAO productDAO;
    private Gson gson;

    @Override
    public void init() {

        userDAO = new UserDAO();
        orderDAO = new OrderDAO();
        productDAO = new ProductDAO();

        gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                new JsonPrimitive(src.toString())
                )
                .create();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
            );
            return;
        }

        String action = request.getParameter("action");

        if ("users".equals(action)) {

            getUsers(response);

        } else if ("orders".equals(action)) {

            getOrders(response);

        } else if ("order-items".equals(action)) {

            getOrderItems(request, response);

        } else if ("products".equals(action)) {

            getProducts(response);

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid admin action"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        if (!isAdmin(request)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Admin access required"
            );
            return;
        }

        String action = request.getParameter("action");

        if ("delete-product".equals(action)) {

            deleteProduct(request, response);

        } else if ("update-order-status".equals(action)) {

            updateOrderStatus(request, response);

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid admin action"
            );
        }
    }

    private boolean isAdmin(HttpServletRequest request) {

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            return false;
        }

        Object role =
                session.getAttribute("role");

        return role != null &&
                "ADMIN".equalsIgnoreCase(
                        role.toString()
                );
    }

    private void getUsers(
            HttpServletResponse response)
            throws IOException {

        List<User> users =
                userDAO.getAllUsers();

        sendJson(response, users);
    }

    private void getOrders(
            HttpServletResponse response)
            throws IOException {

        List<Order> orders =
                orderDAO.getAllOrders();

        sendJson(response, orders);
    }

    private void getOrderItems(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idParameter =
                request.getParameter("id");

        if (idParameter == null ||
                idParameter.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Order ID is required"
            );
            return;
        }

        try {

            int orderId =
                    Integer.parseInt(idParameter);

            if (orderId <= 0) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid order ID"
                );
                return;
            }

            List<OrderItem> items =
                    orderDAO.getOrderItems(orderId);

            sendJson(response, items);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order ID"
            );
        }
    }

    private void getProducts(
            HttpServletResponse response)
            throws IOException {

        List<Product> products =
                productDAO.getAllProducts();

        sendJson(response, products);
    }

    private void deleteProduct(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idParameter =
                request.getParameter("id");

        if (idParameter == null ||
                idParameter.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID is required"
            );
            return;
        }

        try {

            int productId =
                    Integer.parseInt(idParameter);

            if (productId <= 0) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID"
                );
                return;
            }

            boolean deleted =
                    productDAO.deleteProduct(productId);

            sendJson(
                    response,
                    new SuccessResponse(deleted)
            );

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID"
            );
        }
    }

    private void updateOrderStatus(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String idParameter =
                request.getParameter("id");

        String status =
                request.getParameter("status");

        if (idParameter == null ||
                idParameter.isBlank() ||
                status == null ||
                status.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Order ID and status are required"
            );
            return;
        }

        try {

            int orderId =
                    Integer.parseInt(idParameter);

            if (orderId <= 0) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid order ID"
                );
                return;
            }

            status = status.trim().toUpperCase();

            if (!isValidStatus(status)) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid order status"
                );
                return;
            }

            boolean updated =
                    orderDAO.updateOrderStatus(
                            orderId,
                            status
                    );

            sendJson(
                    response,
                    new SuccessResponse(updated)
            );

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order ID"
            );
        }
    }

    private boolean isValidStatus(String status) {

        return "PENDING".equals(status)
                || "CONFIRMED".equals(status)
                || "SHIPPED".equals(status)
                || "DELIVERED".equals(status)
                || "CANCELLED".equals(status);
    }

    private void sendJson(
            HttpServletResponse response,
            Object data)
            throws IOException {

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.getWriter().write(
                gson.toJson(data)
        );
    }

    private static class SuccessResponse {

        private final boolean success;

        private SuccessResponse(boolean success) {
            this.success = success;
        }
    }
}
package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.OrderDAO;
import com.aishwarya.aishwarya_mart.dao.ProductDAO;
import com.aishwarya.aishwarya_mart.model.Order;
import com.aishwarya.aishwarya_mart.model.OrderItem;
import com.aishwarya.aishwarya_mart.service.OrderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private OrderService orderService;
    private OrderDAO orderDAO;
    private Gson gson;

    @Override
    public void init() {

        orderService = new OrderService();
        orderDAO = new OrderDAO();

        gson = new GsonBuilder()
                .registerTypeAdapter(
                        LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>)
                                (src, typeOfSrc, context) ->
                                        new com.google.gson.JsonPrimitive(
                                                src.format(
                                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                                                )
                                        )
                )
                .create();
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

        String role =
                (String) session.getAttribute("role");

        String action =
                request.getParameter("action");

        if ("seller-stats".equals(action)) {

            if (!"SELLER".equals(role) &&
                    !"ADMIN".equals(role)) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Seller or admin access required"
                );
                return;
            }

            sendSellerStats(
                    response,
                    userId
            );

            return;
        }

        if ("items".equals(action)) {

            getOrderItems(
                    request,
                    response,
                    userId,
                    role
            );

            return;
        }

        List<Order> orders;

        if ("SELLER".equals(role)) {

            orders =
                    orderDAO.getOrdersBySeller(userId);

        } else if ("ADMIN".equals(role)) {

            orders =
                    orderDAO.getAllOrders();

        } else {

            orders =
                    orderDAO.getOrdersByUser(userId);
        }

        sendJson(response, orders);
    }

    private void sendSellerStats(
            HttpServletResponse response,
            int sellerId)
            throws IOException {

        int productCount =
                new ProductDAO()
                        .getProductsBySeller(sellerId)
                        .size();

        int orderCount =
                orderDAO.getSellerOrderCount(sellerId);

        BigDecimal revenue =
                orderDAO.getSellerRevenue(sellerId);

        Map<String, Object> stats =
                new HashMap<>();

        stats.put(
                "productCount",
                productCount
        );

        stats.put(
                "orderCount",
                orderCount
        );

        stats.put(
                "revenue",
                revenue
        );

        sendJson(response, stats);
    }

    private void getOrderItems(
            HttpServletRequest request,
            HttpServletResponse response,
            int userId,
            String role)
            throws IOException {

        String orderIdParameter =
                request.getParameter("orderId");

        if (orderIdParameter == null ||
                orderIdParameter.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Order ID is required"
            );
            return;
        }

        try {

            int orderId =
                    Integer.parseInt(orderIdParameter);

            if (orderId <= 0) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid order ID"
                );
                return;
            }

            List<OrderItem> items;

            if ("SELLER".equals(role)) {

                if (!orderDAO.sellerOwnsOrder(
                        orderId,
                        userId)) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You cannot view this order"
                    );
                    return;
                }

                items =
                        orderDAO.getOrderItemsBySeller(
                                orderId,
                                userId
                        );

            } else if ("ADMIN".equals(role)) {

                items =
                        orderDAO.getOrderItems(orderId);

            } else {

                List<Order> userOrders =
                        orderDAO.getOrdersByUser(userId);

                boolean ownsOrder = false;

                for (Order order : userOrders) {

                    if (order.getId() == orderId) {
                        ownsOrder = true;
                        break;
                    }
                }

                if (!ownsOrder) {

                    response.sendError(
                            HttpServletResponse.SC_FORBIDDEN,
                            "You cannot view this order"
                    );
                    return;
                }

                items =
                        orderDAO.getOrderItems(orderId);
            }

            sendJson(response, items);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order ID"
            );
        }
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

        if ("checkout".equals(action)) {

            checkout(
                    userId,
                    response
            );

        } else if ("status".equals(action)) {

            updateStatus(
                    request,
                    response,
                    session
            );

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order action"
            );
        }
    }

    private void checkout(
            int userId,
            HttpServletResponse response)
            throws IOException {

        int orderId =
                orderService.checkout(userId);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding("UTF-8");

        if (orderId == -1) {

            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Checkout failed\"}"
            );

        } else {

            response.getWriter().write(
                    "{\"success\":true,\"orderId\":" +
                            orderId +
                            ",\"message\":\"Order placed successfully\"}"
            );
        }
    }

    private void updateStatus(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session)
            throws IOException {

        String role =
                (String) session.getAttribute("role");

        int userId =
                (Integer) session.getAttribute("userId");

        if (!"ADMIN".equals(role) &&
                !"SELLER".equals(role)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Seller or admin access required"
            );
            return;
        }

        String orderIdParameter =
                request.getParameter("orderId");

        String status =
                request.getParameter("status");

        if (orderIdParameter == null ||
                status == null ||
                status.isBlank()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Order ID and status are required"
            );
            return;
        }

        status =
                status.trim().toUpperCase();

        if (!isValidStatus(status)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order status"
            );
            return;
        }

        try {

            int orderId =
                    Integer.parseInt(orderIdParameter);

            boolean updated;

            if ("ADMIN".equals(role)) {

                updated =
                        orderDAO.updateOrderStatus(
                                orderId,
                                status
                        );

            } else {

                updated =
                        orderDAO.updateOrderStatusBySeller(
                                orderId,
                                userId,
                                status
                        );
            }

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

    private boolean isValidStatus(
            String status) {

        return "PENDING".equals(status) ||
                "CONFIRMED".equals(status) ||
                "SHIPPED".equals(status) ||
                "DELIVERED".equals(status) ||
                "CANCELLED".equals(status);
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

        private SuccessResponse(
                boolean success) {

            this.success = success;
        }
    }
}
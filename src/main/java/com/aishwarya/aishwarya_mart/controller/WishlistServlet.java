package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.WishlistDAO;
import com.aishwarya.aishwarya_mart.model.Product;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/wishlist")
public class WishlistServlet extends HttpServlet {

    private WishlistDAO wishlistDAO;
    private Gson gson;

    @Override
    public void init() {
        wishlistDAO = new WishlistDAO();
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

        List<Product> products =
                wishlistDAO.getWishlistProducts(userId);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.getWriter().write(
                gson.toJson(products)
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

        if (action == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Action is required"
            );
            return;
        }

        /*
         * CLEAR WISHLIST
         *
         * No product ID is needed.
         */
        if ("clear".equals(action)) {

            boolean success =
                    wishlistDAO.clearWishlist(userId);

            sendJsonResponse(response, success);
            return;
        }

        /*
         * ADD / REMOVE
         *
         * These actions require a product ID.
         */
        String productIdParameter =
                request.getParameter("productId");

        if (productIdParameter == null) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID is required"
            );
            return;
        }

        try {

            int productId =
                    Integer.parseInt(productIdParameter);

            if (productId <= 0) {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid product ID"
                );
                return;
            }

            boolean success;

            if ("add".equals(action)) {

                if (wishlistDAO.isInWishlist(
                        userId,
                        productId)) {

                    success = true;

                } else {

                    success =
                            wishlistDAO.addToWishlist(
                                    userId,
                                    productId
                            );
                }

            } else if ("remove".equals(action)) {

                success =
                        wishlistDAO.removeFromWishlist(
                                userId,
                                productId
                        );

            } else {

                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid wishlist action"
                );
                return;
            }

            sendJsonResponse(response, success);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID"
            );
        }
    }

    private void sendJsonResponse(
            HttpServletResponse response,
            boolean success)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> result =
                new HashMap<>();

        result.put("success", success);

        response.getWriter().write(
                gson.toJson(result)
        );
    }
}
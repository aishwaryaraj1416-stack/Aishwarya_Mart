package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.ReviewDAO;
import com.aishwarya.aishwarya_mart.model.Review;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/reviews")
public class ReviewServlet extends HttpServlet {

    private final ReviewDAO reviewDAO = new ReviewDAO();

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(
                    LocalDateTime.class,
                    (com.google.gson.JsonSerializer<LocalDateTime>)
                            (src, typeOfSrc, context) ->
                                    new com.google.gson.JsonPrimitive(
                                            src.toString()
                                    )
            )
            .create();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String productIdParam =
                request.getParameter("productId");

        if (productIdParam == null) {
            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"error\":\"Product ID is required\"}"
            );
            return;
        }

        try {

            int productId =
                    Integer.parseInt(productIdParam);

            var reviews =
                    reviewDAO.getReviewsByProduct(productId);

            double average =
                    reviewDAO.getAverageRating(productId);

            int count =
                    reviewDAO.getReviewCount(productId);

            var result = new java.util.HashMap<String, Object>();

            result.put("reviews", reviews);
            result.put("rating", average);
            result.put("reviewCount", count);

            response.getWriter().write(
                    gson.toJson(result)
            );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"error\":\"Invalid product ID\"}"
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);

            response.getWriter().write(
                    "{\"error\":\"Please login first\"}"
            );
            return;
        }

        String role =
                (String) session.getAttribute("role");

        if (!"BUYER".equalsIgnoreCase(role)) {

            response.setStatus(
                    HttpServletResponse.SC_FORBIDDEN);

            response.getWriter().write(
                    "{\"error\":\"Only buyers can write reviews\"}"
            );
            return;
        }

        try {

            int userId =
                    (Integer) session.getAttribute("userId");

            int productId =
                    Integer.parseInt(
                            request.getParameter("productId")
                    );

            int rating =
                    Integer.parseInt(
                            request.getParameter("rating")
                    );

            String comment =
                    request.getParameter("comment");

            if (rating < 1 || rating > 5) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "{\"error\":\"Rating must be between 1 and 5\"}"
                );
                return;
            }

            if (comment == null) {
                comment = "";
            }

            comment = comment.trim();

            if (comment.length() > 1000) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "{\"error\":\"Comment is too long\"}"
                );
                return;
            }

            if (!reviewDAO.hasDeliveredProduct(
                    userId,
                    productId)) {

                response.setStatus(
                        HttpServletResponse.SC_FORBIDDEN);

                response.getWriter().write(
                        "{\"error\":\"You can review a product only after it is delivered\"}"
                );
                return;
            }

            if (reviewDAO.hasReviewed(
                    userId,
                    productId)) {

                response.setStatus(
                        HttpServletResponse.SC_CONFLICT);

                response.getWriter().write(
                        "{\"error\":\"You have already reviewed this product\"}"
                );
                return;
            }

            Review review = new Review();

            review.setProductId(productId);
            review.setUserId(userId);
            review.setRating(rating);
            review.setComment(comment);

            boolean success =
                    reviewDAO.addReview(review);

            if (!success) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST);

                response.getWriter().write(
                        "{\"error\":\"Could not add review\"}"
                );
                return;
            }

            response.getWriter().write(
                    "{\"success\":true,\"message\":\"Review added successfully\"}"
            );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST);

            response.getWriter().write(
                    "{\"error\":\"Invalid product ID or rating\"}"
            );

        } catch (Exception e) {

            e.printStackTrace();

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.getWriter().write(
                    "{\"error\":\"Something went wrong\"}"
            );
        }
    }
}
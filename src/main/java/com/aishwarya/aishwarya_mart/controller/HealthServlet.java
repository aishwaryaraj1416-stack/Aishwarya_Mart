package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.util.DBUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@WebServlet("/api/v1/health")
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        boolean databaseUp = false;

        try (Connection connection = DBUtil.getConnection()) {

            databaseUp = connection != null
                    && !connection.isClosed();

        } catch (Exception e) {
            databaseUp = false;
        }

        if (databaseUp) {

            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().write(
                    "{\"status\":\"UP\",\"db\":\"UP\"}"
            );

        } else {

            response.setStatus(
                    HttpServletResponse.SC_SERVICE_UNAVAILABLE
            );

            response.getWriter().write(
                    "{\"status\":\"DOWN\",\"db\":\"DOWN\"}"
            );
        }
    }
}
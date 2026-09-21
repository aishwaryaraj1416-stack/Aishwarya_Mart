package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.dao.UserDAO;
import com.aishwarya.aishwarya_mart.model.User;
import com.aishwarya.aishwarya_mart.util.PasswordUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private UserDAO userDAO;
    private Gson gson;

    @Override
    public void init() {
        userDAO = new UserDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");

        if ("me".equals(action)) {

            getCurrentUser(request, response);

        } else if ("logout".equals(action)) {

            logout(request, response);

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action"
            );
        }
    }

    private void getCurrentUser(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        Map<String, Object> result =
                new HashMap<>();

        if (session == null ||
                session.getAttribute("userId") == null) {

            result.put(
                    "loggedIn",
                    false
            );

        } else {

            result.put(
                    "loggedIn",
                    true
            );

            result.put(
                    "userId",
                    session.getAttribute("userId")
            );

            result.put(
                    "role",
                    session.getAttribute("role")
            );

            User user =
                    (User) session.getAttribute("user");

            if (user != null) {

                result.put(
                        "name",
                        user.getName()
                );

                result.put(
                        "email",
                        user.getEmail()
                );
            }
        }

        response.getWriter().write(
                gson.toJson(result)
        );
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action =
                request.getParameter("action");

        if ("register".equals(action)) {

            register(request, response);

        } else if ("login".equals(action)) {

            login(request, response);

        } else if ("logout".equals(action)) {

            logout(request, response);

        } else {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action"
            );
        }
    }

    private void register(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String name =
                request.getParameter("name");

        String email =
                request.getParameter("email");

        String password =
                request.getParameter("password");

        String role =
                request.getParameter("role");

        if (name == null ||
                email == null ||
                password == null ||
                name.isBlank() ||
                email.isBlank() ||
                password.isBlank()) {

            response.sendRedirect(
                    "register.html?error=missing"
            );

            return;
        }

        name = name.trim();
        email = email.trim().toLowerCase();

        if (password.length() < 6) {

            response.sendRedirect(
                    "register.html?error=weak"
            );

            return;
        }

        if (!"BUYER".equals(role) &&
                !"SELLER".equals(role)) {

            role = "BUYER";
        }

        if (userDAO.emailExists(email)) {

            response.sendRedirect(
                    "register.html?error=exists"
            );

            return;
        }

        String passwordHash =
                PasswordUtil.hash(password);

        User user =
                new User(
                        name,
                        email,
                        passwordHash,
                        role
                );

        boolean registered =
                userDAO.register(user);

        if (registered) {

            response.sendRedirect(
                    "login.html?registered=true"
            );

        } else {

            response.sendRedirect(
                    "register.html?error=failed"
            );
        }
    }

    private void login(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String email =
                request.getParameter("email");

        String password =
                request.getParameter("password");

        if (email == null ||
                password == null ||
                email.isBlank() ||
                password.isBlank()) {

            response.sendRedirect(
                    "login.html?error=missing"
            );

            return;
        }

        email = email.trim().toLowerCase();

        User user =
                userDAO.findByEmail(email);

        if (user == null ||
                !PasswordUtil.matches(
                        password,
                        user.getPasswordHash())) {

            response.sendRedirect(
                    "login.html?error=invalid"
            );

            return;
        }

        HttpSession oldSession =
                request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        HttpSession session =
                request.getSession(true);

        session.setMaxInactiveInterval(
                30 * 60
        );

        session.setAttribute(
                "user",
                user
        );

        session.setAttribute(
                "userId",
                user.getId()
        );

        session.setAttribute(
                "role",
                user.getRole()
        );

        if ("ADMIN".equalsIgnoreCase(
                user.getRole())) {

            response.sendRedirect(
                    "admin.html"
            );

        } else if ("SELLER".equalsIgnoreCase(
                user.getRole())) {

            response.sendRedirect(
                    "seller.html"
            );

        } else {

            response.sendRedirect(
                    "index.html"
            );
        }
    }

    private void logout(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                "login.html?logout=true"
        );
    }
}
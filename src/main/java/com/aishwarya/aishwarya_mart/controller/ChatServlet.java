package com.aishwarya.aishwarya_mart.controller;

import com.aishwarya.aishwarya_mart.service.ChatService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {
    private final ChatService chatService = new ChatService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
                if (body.length() > 10000) {
                    sendError(response, 413, "That message is too large. Please shorten it.");
                    return;
                }
            }
        }

        try {
            JsonObject json = JsonParser.parseString(body.toString()).getAsJsonObject();
            String message = json.has("message") && !json.get("message").isJsonNull()
                    ? json.get("message").getAsString().trim() : "";

            if (message.isEmpty()) {
                sendError(response, 400, "Type a message first.");
                return;
            }
            if (message.length() > 500) {
                sendError(response, 400, "Please keep messages under 500 characters.");
                return;
            }

            Integer userId = null;
            HttpSession session = request.getSession(false);
            if (session != null) {
                Object value = session.getAttribute("userId");
                if (value instanceof Number) userId = ((Number) value).intValue();
            }

            JsonObject data = new JsonObject();
            data.addProperty("reply", chatService.getReply(message, userId));
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.add("data", data);
            result.add("error", com.google.gson.JsonNull.INSTANCE);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(result.toString());
        } catch (Exception e) {
            sendError(response, 400, "I couldn't read that message. Please try again.");
        }
    }

    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        JsonObject error = new JsonObject();
        error.addProperty("code", "CHAT_ERROR");
        error.addProperty("message", message);
        JsonObject result = new JsonObject();
        result.addProperty("success", false);
        result.add("data", com.google.gson.JsonNull.INSTANCE);
        result.add("error", error);
        response.getWriter().write(result.toString());
    }
}
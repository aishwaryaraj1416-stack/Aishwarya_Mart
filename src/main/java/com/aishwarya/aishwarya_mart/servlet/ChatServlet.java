package com.aishwarya.aishwarya_mart.servlet;

import com.aishwarya.aishwarya_mart.service.ChatService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

@WebServlet("/api/v1/chat")
public class ChatServlet extends HttpServlet {
    private static final Gson GSON = new Gson();
    private final ChatService chatService = new ChatService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json; charset=UTF-8");

        JsonObject result = new JsonObject();
        try {
            JsonObject body = GSON.fromJson(request.getReader(), JsonObject.class);
            String message = body == null || !body.has("message")
                    || body.get("message").isJsonNull()
                    ? "" : body.get("message").getAsString().trim();

            if (message.isBlank()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("success", false);
                JsonObject error = new JsonObject();
                error.addProperty("message", "Type a message first.");
                result.add("error", error);
            } else if (message.length() > 1000) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                result.addProperty("success", false);
                JsonObject error = new JsonObject();
                error.addProperty("message", "Please keep your message under 1,000 characters.");
                result.add("error", error);
            } else {
                Integer userId = getSignedInUserId(request.getSession(false));
                String reply = chatService.getReply(message, userId);
                result.addProperty("success", true);
                JsonObject data = new JsonObject();
                data.addProperty("reply", reply);
                result.add("data", data);
            }
        } catch (Exception e) {
            getServletContext().log("Boba chat request failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            result.addProperty("success", false);
            JsonObject error = new JsonObject();
            error.addProperty("message", "Boba had a little hiccup. Please try again.");
            result.add("error", error);
        }
        response.getWriter().write(GSON.toJson(result));
    }

    private Integer getSignedInUserId(HttpSession session) {
        if (session == null) return null;

        String[] idKeys = {"userId", "user_id", "loggedInUserId", "currentUserId"};
        for (String key : idKeys) {
            Object value = session.getAttribute(key);
            Integer id = toPositiveInteger(value);
            if (id != null) return id;
        }

        String[] userKeys = {"user", "currentUser", "loggedInUser", "current_user"};
        for (String key : userKeys) {
            Object user = session.getAttribute(key);
            if (user == null) continue;
            Integer id = invokeIdGetter(user, "getId");
            if (id == null) id = invokeIdGetter(user, "getUserId");
            if (id != null) return id;
        }
        return null;
    }

    private Integer toPositiveInteger(Object value) {
        if (value instanceof Number number && number.intValue() > 0) return number.intValue();
        if (value instanceof String text) {
            try {
                int id = Integer.parseInt(text.trim());
                return id > 0 ? id : null;
            } catch (NumberFormatException ignored) { }
        }
        return null;
    }

    private Integer invokeIdGetter(Object value, String methodName) {
        try {
            Method method = value.getClass().getMethod(methodName);
            return toPositiveInteger(method.invoke(value));
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }
}

package com.aishwarya.aishwarya_mart.service;

import com.aishwarya.aishwarya_mart.dao.CartDAO;
import com.aishwarya.aishwarya_mart.dao.ProductDAO;
import com.aishwarya.aishwarya_mart.model.CartItem;
import com.aishwarya.aishwarya_mart.model.Product;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatService {
    private final ProductDAO productDAO = new ProductDAO();
    private final CartDAO cartDAO = new CartDAO();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8)).build();

    public String getReply(String message, Integer userId) {
        String text = message.trim().toLowerCase(Locale.ROOT);

        if (text.matches(".*\\b(hi|hello|hey|hiya|hiii+)\\b.*")) {
            return "Hiii! 🧋 I'm Boba, your Aishwarya Mart shopping buddy! Ask me to find products, suggest items within a budget, or help with your cart. What are we shopping for today?";
        }
        if (text.contains("what can you do") || text.equals("help") || text.contains("help me")) {
            return "Here's what I can help with! 🛍️\n• Find products by name or category\n• Suggest in-stock products within your budget\n• Add products to your cart\n• Remove products from your cart\n• Show your cart and total\n• Answer general shopping questions with AI\n\nTry: “show products under 1000” or “find skincare”.";
        }
        if (text.matches(".*\\b(my cart|show cart|show my cart|view cart|view my cart|cart total|what is in my cart|basket)\\b.*")
                || text.equals("cart") || text.equals("show my bag")) {
            return showCart(userId);
        }
        if (text.contains("remove") || text.contains("delete") || text.contains("take out")) {
            return removeProduct(message, userId);
        }
        if (text.contains("add") && (text.contains("cart") || text.contains("basket") || text.contains("bag"))) {
            return addProduct(message, userId);
        }

        BigDecimal budget = findBudget(text);
        if (budget != null) return recommendWithinBudget(budget);

        if (text.contains("show products") || text.contains("all products") || text.contains("browse products")
                || text.contains("find ") || text.startsWith("search ") || text.startsWith("show me ")
                || text.startsWith("looking for ") || text.startsWith("i want ") || text.startsWith("browse ")
                || text.startsWith("search for ") || text.startsWith("products in ") || text.startsWith("show ")) {
            return searchProducts(message);
        }

        String aiReply = askGemini(message);
        if (aiReply != null && !aiReply.isBlank()) return aiReply;
        return "I'm having trouble reaching my AI brain right now 🧋 You can still ask me to find products, check your budget, or manage your cart. Try again in a little bit!";
    }

    private String searchProducts(String message) {
        String keyword = message.toLowerCase(Locale.ROOT)
                .replaceAll("\\b(show me|show|find|search for|search|browse|products|product|items|item|i want|looking for|please|shop|in|category)\\b", " ")
                .replaceAll("\\s+", " ").trim();
        List<Product> found = keyword.isBlank() ? productDAO.getAllProducts() : productDAO.searchProducts(keyword);
        if (found.isEmpty()) return "I couldn't find a matching product 🥺 Try another name or category, like “fashion”, “beauty”, or “electronics”.";
        return formatProducts(found, keyword.isBlank() ? "Here are some products from Aishwarya Mart 🛍️" : "Here's what I found for you 🔎");
    }

    private String recommendWithinBudget(BigDecimal budget) {
        if (budget.signum() <= 0) return "Tell me a budget greater than ₹0 and I'll look for options 🧋";
        List<Product> affordable = new ArrayList<>();
        for (Product p : productDAO.getAllProducts()) {
            if (p.getPrice() != null && p.getPrice().compareTo(budget) <= 0 && p.getStockQty() > 0) affordable.add(p);
        }
        if (affordable.isEmpty()) return "I couldn't find in-stock products within ₹" + budget.toPlainString() + ". Want to try a slightly higher budget? 💗";
        return formatProducts(affordable, "Cute finds within ₹" + budget.toPlainString() + " 💸");
    }

    private String addProduct(String message, Integer userId) {
        if (userId == null) return "Please log in first so I can add items to your cart 💗";
        String query = cleanProductQuery(message);
        int quantity = 1;
        Matcher matcher = Pattern.compile("\\b(\\d+)\\s*(?:x\\s*)?(?:of\\s+)?").matcher(query);
        if (matcher.find()) {
            try {
                quantity = Integer.parseInt(matcher.group(1));
                query = query.substring(matcher.end()).trim();
            } catch (NumberFormatException e) {
                return "Tell me a valid quantity, please.";
            }
        }
        if (quantity < 1 || quantity > 100) return "Please choose a quantity between 1 and 100.";
        if (query.isBlank()) return "Which product should I add? Tell me its name, like “add the classic watch to my cart”.";
        List<Product> matches = productDAO.searchProducts(query);
        Product chosen = null;
        for (Product p : matches) if (p.getName() != null && p.getName().equalsIgnoreCase(query)) { chosen = p; break; }
        if (chosen == null && matches.size() == 1) chosen = matches.get(0);
        if (chosen == null) return matches.isEmpty() ? "I couldn't find that product. Try its full name or ask me to show products 🌷" : formatProducts(matches, "I found a few matches. Which one did you mean?");
        if (chosen.getStockQty() < quantity) return "Only " + chosen.getStockQty() + " unit(s) of " + chosen.getName() + " are in stock right now.";
        int current = 0;
        for (CartItem item : cartDAO.getCartItems(userId)) if (item.getProductId() == chosen.getId()) current = item.getQuantity();
        if (current + quantity > chosen.getStockQty()) return "Your cart already has " + current + " of " + chosen.getName() + ". There isn't enough stock for that quantity.";
        boolean ok = cartDAO.addToCart(userId, chosen.getId(), quantity);
        return ok ? "Done! 🛒 Added " + quantity + " × " + chosen.getName() + " to your bag." : "I couldn't update your cart. Please try again.";
    }

    private String removeProduct(String message, Integer userId) {
        if (userId == null) return "Please log in first to manage your cart 💗";
        String query = cleanProductQuery(message);
        if (query.isBlank()) return "Which product should I remove? Tell me its name.";
        List<CartItem> matches = new ArrayList<>();
        for (CartItem item : cartDAO.getCartItems(userId)) {
            if (item.getProductName() != null && item.getProductName().toLowerCase(Locale.ROOT).contains(query)) matches.add(item);
        }
        if (matches.isEmpty()) return "I couldn't find that item in your cart. Ask “show my cart” to see what's there.";
        if (matches.size() > 1) return "I found more than one matching item. Please tell me the exact product name.";
        CartItem item = matches.get(0);
        return cartDAO.removeFromCart(userId, item.getProductId())
                ? "Done! 🧺 Removed " + item.getProductName() + " from your cart."
                : "I couldn't remove that item. Please try again.";
    }

    private String showCart(Integer userId) {
        if (userId == null) return "Please log in to view your personal cart 💗";
        List<CartItem> items = cartDAO.getCartItems(userId);
        if (items.isEmpty()) return "Your cart is empty for now 🌷 Want me to help you find something?";
        StringBuilder out = new StringBuilder("Here's your bag 🛒\n");
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            out.append("• ").append(item.getProductName()).append(" × ").append(item.getQuantity())
                    .append(" — ₹").append(subtotal.toPlainString()).append('\n');
        }
        return out.append("\nBag total: ₹").append(total.toPlainString()).toString();
    }

    private String formatProducts(List<Product> products, String heading) {
        if (products == null || products.isEmpty()) return "I couldn't find matching products. Try another name or category 🌷";
        StringBuilder out = new StringBuilder(heading).append('\n');
        int shown = 0;
        for (Product p : products) {
            if (shown++ == 5) { out.append("And more products are available on the site!"); break; }
            out.append("• ").append(p.getName()).append(" — ₹")
                    .append(p.getPrice() == null ? "price unavailable" : p.getPrice().toPlainString());
            if (p.getStockQty() <= 0) out.append(" (out of stock)");
            out.append('\n');
        }
        return out.toString().trim();
    }

    private String cleanProductQuery(String message) {
        return message.toLowerCase(Locale.ROOT)
                .replaceAll("\\b(please|can you|could you|would you|add|remove|delete|take out|from|my|the|cart|basket|bag|to|of|product|item|in)\\b", " ")
                .replaceAll("\\b\\d+\\b", " ").replaceAll("\\s+", " ").trim();
    }

    private BigDecimal findBudget(String text) {
        Matcher matcher = Pattern.compile("(?:under|below|within|less than|budget of|up to)\\s*(?:₹|rs\\.?\\s*)?(\\d+(?:\\.\\d{1,2})?)|₹\\s*(\\d+(?:\\.\\d{1,2})?)").matcher(text);
        if (!matcher.find()) return null;
        try { return new BigDecimal(matcher.group(1) != null ? matcher.group(1) : matcher.group(2)); }
        catch (NumberFormatException e) { return null; }
    }

    private String askGemini(String message) {
        String key = System.getenv("GEMINI_API_KEY");
        if (key == null || key.isBlank()) key = System.getProperty("GEMINI_API_KEY");
        if (key == null || key.isBlank()) {
            return "My AI connection needs to be set up first 🧋 The shopping tools still work! Ask me to find products, suggest items under a budget, or show your cart.";
        }

        try {
            JsonObject requestBody = new JsonObject();
            JsonObject systemPart = new JsonObject();
            systemPart.addProperty("text", "You are Boba, the friendly, concise AI shopping buddy for Aishwarya Mart. Be warm, useful and clear. Help with shopping, product selection, gift ideas, styling and general questions. Never invent Aishwarya Mart product names, prices, stock, order status, or claim you changed the cart. For live catalog/cart actions, ask the user to use Boba's product and cart commands. Do not ask for passwords, payment details, or API keys. Use short answers and occasional tasteful emojis.");
            JsonArray systemParts = new JsonArray();
            systemParts.add(systemPart);
            JsonObject systemInstruction = new JsonObject();
            systemInstruction.add("parts", systemParts);
            requestBody.add("systemInstruction", systemInstruction);

            JsonObject userPart = new JsonObject();
            userPart.addProperty("text", message);
            JsonArray parts = new JsonArray();
            parts.add(userPart);
            JsonObject content = new JsonObject();
            content.add("parts", parts);
            JsonArray contents = new JsonArray();
            contents.add(content);
            requestBody.add("contents", contents);
            JsonObject generation = new JsonObject();
            generation.addProperty("temperature", 0.7);
            generation.addProperty("maxOutputTokens", 400);
            requestBody.add("generationConfig", generation);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + URLEncoder.encode(key, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                System.err.println("Boba Gemini request failed with HTTP " + response.statusCode());
                return "My AI brain is taking a tiny break 🧋 Please try again soon, or ask me to search products and manage your cart.";
            }
            JsonObject parsed = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray candidates = parsed.has("candidates") ? parsed.getAsJsonArray("candidates") : new JsonArray();
            if (candidates.isEmpty()) return null;
            JsonObject first = candidates.get(0).getAsJsonObject();
            if (!first.has("content")) return null;
            JsonArray replyParts = first.getAsJsonObject("content").getAsJsonArray("parts");
            StringBuilder reply = new StringBuilder();
            for (int i = 0; i < replyParts.size(); i++) {
                JsonObject part = replyParts.get(i).getAsJsonObject();
                if (part.has("text")) reply.append(part.get("text").getAsString());
            }
            return reply.toString().trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "I got interrupted while thinking 🧋 Please send that again.";
        } catch (IOException | RuntimeException e) {
            System.err.println("Boba Gemini connection failed: " + e.getClass().getSimpleName());
            return "I couldn't reach my AI brain just now 🧋 Please try again in a moment.";
        }
    }
}
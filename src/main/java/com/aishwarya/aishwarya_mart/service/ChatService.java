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
        String text = message.toLowerCase(Locale.ROOT).trim();

        if (text.matches(".*\\b(hi|hello|hey|hiya|hii)\\b.*")) {
            return "Hiii! 🧋 I'm Boba, your Aishwarya Mart shopping buddy! "
                    + "I can help you discover products, find options within a budget, "
                    + "and manage your cart. What are we shopping for today?";
        }
        if (text.contains("help") || text.contains("what can you do")) {
            return "I'm Boba 🧋 Here's what I can help with:\n"
                    + "• Find products by name or category\n"
                    + "• Suggest in-stock products under a budget\n"
                    + "• Add a product to your cart (sign-in required)\n"
                    + "• Remove a product from your cart\n"
                    + "• Show your cart and total\n"
                    + "• Answer shopping questions using the current catalogue\n\n"
                    + "Try: “bags under ₹500” or “show my cart”.";
        }
        if (isCartRequest(text)) return showCart(userId);
        if (text.contains("remove") || text.contains("delete") || text.contains("take out")) {
            return removeProduct(message, userId);
        }
        if (text.contains("add") && (text.contains("cart") || text.contains("bag") || text.contains("basket"))) {
            return addProduct(message, userId);
        }

        BigDecimal budget = findBudget(text);
        if (budget != null) {
            List<Product> products = new ArrayList<>();
            for (Product product : productDAO.getAllProducts()) {
                if (product.getPrice() != null && product.getPrice().signum() >= 0
                        && product.getPrice().compareTo(budget) <= 0
                        && product.getStockQty() > 0) products.add(product);
            }
            return formatProducts(products, "Cute finds within ₹" + budget.toPlainString() + " ✨");
        }

        if (looksLikeProductSearch(text)) {
            String query = cleanSearchQuery(message);
            List<Product> products = query.isBlank()
                    ? productDAO.getAllProducts() : productDAO.searchProducts(query);
            return formatProducts(products, query.isBlank()
                    ? "Here are some things you can explore 🛍️"
                    : "I found these for you 🔎");
        }

        String aiReply = askGemini(message);
        if (aiReply != null && !aiReply.isBlank()) return aiReply;
        return "I'm here to help you shop! 🧋 Try asking me to find a product, "
                + "show items under ₹500, or show your cart. "
                + "For a general shopping question, please try again in a moment.";
    }

    private boolean isCartRequest(String text) {
        return (text.contains("cart") || text.contains("basket") || text.contains("my bag"))
                && (text.contains("show") || text.contains("view") || text.contains("what")
                || text.contains("my") || text.contains("total") || text.contains("items")
                || text.equals("cart") || text.equals("basket") || text.equals("bag"));
    }

    private boolean looksLikeProductSearch(String text) {
        return text.contains("product") || text.contains("shop") || text.contains("search")
                || text.contains("find") || text.contains("show me") || text.contains("browse")
                || text.startsWith("i want") || text.startsWith("looking for")
                || text.startsWith("do you have") || text.startsWith("any ");
    }

    private String addProduct(String message, Integer userId) {
        if (userId == null) return "Please sign in first, then I can add things to your cart. 💗";
        String query = cleanActionQuery(message);
        int quantity = 1;
        Matcher number = Pattern.compile("\\b(\\d+)\\b").matcher(query);
        if (number.find()) {
            try { quantity = Integer.parseInt(number.group(1)); }
            catch (NumberFormatException e) { return "Please enter a valid quantity."; }
            query = (query.substring(0, number.start()) + " " + query.substring(number.end()))
                    .replaceAll("\\s+", " ").trim();
        }
        if (quantity < 1 || quantity > 100) return "Choose a quantity from 1 to 100, please.";
        if (query.isBlank()) return "Which product should I add? Tell me its name. 🛍️";

        List<Product> matches = productDAO.searchProducts(query);
        Product chosen = chooseProduct(matches, query);
        if (chosen == null) {
            return matches.isEmpty() ? "I couldn't find that product. Try its name or category. 🌷"
                    : formatProducts(matches, "I found a few matches. Which one did you mean?");
        }
        if (chosen.getStockQty() < quantity) {
            return "Only " + chosen.getStockQty() + " unit(s) of " + chosen.getName() + " are in stock.";
        }
        int current = 0;
        for (CartItem item : cartDAO.getCartItems(userId)) {
            if (item.getProductId() == chosen.getId()) current = item.getQuantity();
        }
        if (current + quantity > chosen.getStockQty()) {
            return "Your cart already has " + current + " of " + chosen.getName()
                    + ". There are only " + chosen.getStockQty() + " in stock.";
        }
        return cartDAO.addToCart(userId, chosen.getId(), quantity)
                ? "Done! 🛒 Added " + quantity + " × " + chosen.getName() + " to your cart."
                : "I couldn't update your cart just now. Please try again.";
    }

    private String removeProduct(String message, Integer userId) {
        if (userId == null) return "Please sign in first to manage your cart. 💗";
        String query = cleanActionQuery(message);
        if (query.isBlank()) return "Which product would you like me to remove?";
        List<CartItem> matches = new ArrayList<>();
        for (CartItem item : cartDAO.getCartItems(userId)) {
            if (item.getProductName() != null
                    && item.getProductName().toLowerCase(Locale.ROOT).contains(query)) matches.add(item);
        }
        if (matches.isEmpty()) return "I couldn't find that item in your cart. Ask me to show your cart.";
        if (matches.size() > 1) {
            StringBuilder options = new StringBuilder("I found a few matches in your cart:\n");
            for (CartItem item : matches) options.append("• ").append(item.getProductName()).append('\n');
            return options + "Tell me the exact product name, please.";
        }
        CartItem item = matches.get(0);
        return cartDAO.removeFromCart(userId, item.getProductId())
                ? "All done! 🧺 Removed " + item.getProductName() + " from your cart."
                : "I couldn't remove that item. Please try again.";
    }

    private String showCart(Integer userId) {
        if (userId == null) return "Please sign in to view your personal cart. 💗";
        List<CartItem> items = cartDAO.getCartItems(userId);
        if (items.isEmpty()) return "Your cart is empty for now! 🌷 Let's find something you love.";
        StringBuilder reply = new StringBuilder("Your cart 🛒\n");
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : items) {
            BigDecimal price = item.getPrice() == null ? BigDecimal.ZERO : item.getPrice();
            BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
            total = total.add(subtotal);
            reply.append("• ").append(item.getProductName()).append(" × ")
                    .append(item.getQuantity()).append(" — ₹").append(subtotal.toPlainString()).append('\n');
        }
        return reply.append("\nCart total: ₹").append(total.toPlainString()).toString();
    }

    private String formatProducts(List<Product> products, String heading) {
        if (products == null || products.isEmpty()) {
            return "I couldn't find matching in-stock products. Try another name, category, or budget. 🌷";
        }
        StringBuilder reply = new StringBuilder(heading).append('\n');
        int shown = 0;
        for (Product product : products) {
            if (shown++ == 5) {
                reply.append("And more products are available on Aishwarya Mart!");
                break;
            }
            reply.append("• ").append(product.getName()).append(" — ₹")
                    .append(product.getPrice() == null ? "price unavailable" : product.getPrice().toPlainString());
            if (product.getStockQty() <= 0) reply.append(" (out of stock)");
            if (product.getCategory() != null && !product.getCategory().isBlank()) {
                reply.append(" · ").append(product.getCategory());
            }
            reply.append('\n');
        }
        return reply.toString().trim();
    }

    private Product chooseProduct(List<Product> products, String query) {
        if (products == null || products.isEmpty()) return null;
        for (Product product : products) {
            if (product.getName() != null && product.getName().equalsIgnoreCase(query.trim())) return product;
        }
        return products.size() == 1 ? products.get(0) : null;
    }

    private String cleanActionQuery(String message) {
        return message.toLowerCase(Locale.ROOT)
                .replaceAll("\\b(please|can you|could you|would you|add|remove|delete|take out|"
                        + "from|my|the|cart|basket|bag|to|of|product|item|items|quantity)\\b", " ")
                .replaceAll("\\b\\d+\\b", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private String cleanSearchQuery(String message) {
        return message.toLowerCase(Locale.ROOT)
                .replaceAll("\\b(show me|show|find|search for|search|browse|products|product|items|item|"
                        + "i want|looking for|please|shop|do you have|any)\\b", " ")
                .replaceAll("\\s+", " ").trim();
    }

    private BigDecimal findBudget(String text) {
        Matcher matcher = Pattern.compile("(?:under|below|within|less than|budget of|upto|up to)\\s*(?:₹|rs\\.?\\s*)?(\\d+(?:\\.\\d{1,2})?)|₹\\s*(\\d+(?:\\.\\d{1,2})?)")
                .matcher(text);
        if (!matcher.find()) return null;
        try {
            return new BigDecimal(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String askGemini(String message) {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) return null;

        StringBuilder catalogue = new StringBuilder();
        List<Product> products = productDAO.getAllProducts();
        for (int i = 0; i < Math.min(products.size(), 60); i++) {
            Product p = products.get(i);
            catalogue.append("- ").append(p.getName())
                    .append(" | category: ").append(p.getCategory())
                    .append(" | price: ₹").append(p.getPrice())
                    .append(" | stock: ").append(p.getStockQty()).append('\n');
        }

        String prompt = "You are Boba, the friendly and concise shopping assistant for Aishwarya Mart. "
                + "Use a warm, playful tone with occasional emojis. Answer the user's shopping questions "
                + "using only the catalogue below for factual product, price, category, and stock claims. "
                + "Never invent products, prices, discounts, policies, or order information. "
                + "You cannot perform cart actions here; the app handles those separately. "
                + "If the catalogue doesn't answer a question, say so clearly.\n\nCATALOGUE:\n"
                + catalogue + "\nUSER MESSAGE:\n" + message;

        try {
            JsonObject body = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", prompt);
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            body.add("contents", contents);
            JsonObject generation = new JsonObject();
            generation.addProperty("temperature", 0.5);
            generation.addProperty("maxOutputTokens", 350);
            body.add("generationConfig", generation);

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body.toString(), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = http.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) return null;

            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray candidates = json.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) return null;
            JsonArray responseParts = candidates.get(0).getAsJsonObject()
                    .getAsJsonObject("content").getAsJsonArray("parts");
            if (responseParts == null || responseParts.isEmpty()) return null;
            String answer = responseParts.get(0).getAsJsonObject().get("text").getAsString().trim();
            return answer.length() > 2500 ? answer.substring(0, 2500) : answer;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }
}

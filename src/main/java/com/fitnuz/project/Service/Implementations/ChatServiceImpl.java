package com.fitnuz.project.Service.Implementations;

import com.fitnuz.project.Model.Product;
import com.fitnuz.project.Payload.DTO.ChatMessageDto;
import com.fitnuz.project.Repository.ProductRepository;
import com.fitnuz.project.Service.Definations.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent}")
    private String geminiApiUrl;

    @Override
    public String getReply(String userMessage, List<ChatMessageDto> history) {
        String catalog = buildProductCatalog();
        String systemPrompt = buildSystemPrompt(catalog);

        Map<String, Object> requestBody = buildGeminiRequest(systemPrompt, history, userMessage);

        String url = geminiApiUrl + "?key=" + geminiApiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            return extractReply(response.getBody());
        } catch (Exception e) {
            return "Sorry, I'm having trouble connecting right now. Please try again in a moment.";
        }
    }

    private String buildProductCatalog() {
        List<Product> products = productRepository.findAll();

        if (products.isEmpty()) {
            return "No products are currently available in our store.";
        }

        Map<String, List<Product>> grouped = products.stream()
                .collect(Collectors.groupingBy(p ->
                        p.getCategory() != null ? p.getCategory().getCategoryName() : "Uncategorized"));

        StringBuilder sb = new StringBuilder();
        sb.append("=== FitNuz Product Catalog ===\n\n");

        for (Map.Entry<String, List<Product>> entry : grouped.entrySet()) {
            sb.append("Category: ").append(entry.getKey()).append("\n");
            for (Product p : entry.getValue()) {
                sb.append("  - ").append(p.getProductName());
                sb.append(" | Price: $").append(String.format("%.2f", p.getProductPrice()));
                if (p.getDiscount() != null && p.getDiscount() > 0) {
                    sb.append(" | Discount: ").append(String.format("%.0f", p.getDiscount())).append("%");
                    sb.append(" | Sale Price: $").append(String.format("%.2f", p.getSpecialPrice()));
                }
                if (p.getProductStock() != null && p.getProductStock() <= 0) {
                    sb.append(" | OUT OF STOCK");
                }
                if (p.getProductDescription() != null && !p.getProductDescription().isEmpty()) {
                    sb.append(" | ").append(p.getProductDescription());
                }
                sb.append("\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    private String buildSystemPrompt(String catalog) {
        return "You are FitNuz Assistant, a friendly and knowledgeable shopping assistant for FitNuz, " +
                "a fitness e-commerce store. Your job is to help customers find products, compare options, " +
                "and answer questions about pricing, discounts, and availability.\n\n" +
                "Guidelines:\n" +
                "- Be concise, friendly, and helpful\n" +
                "- Only recommend products from the catalog below\n" +
                "- If a product is out of stock, let the customer know\n" +
                "- Mention discounts and sale prices when relevant\n" +
                "- If asked about something not in the catalog, politely say it's not currently available\n" +
                "- Do not make up products or prices\n" +
                "- Keep responses short (2-4 sentences) unless the customer asks for details\n\n" +
                catalog;
    }

    private Map<String, Object> buildGeminiRequest(String systemPrompt, List<ChatMessageDto> history, String userMessage) {
        Map<String, Object> request = new HashMap<>();

        // System instruction
        Map<String, Object> systemInstruction = new HashMap<>();
        Map<String, String> systemPart = new HashMap<>();
        systemPart.put("text", systemPrompt);
        systemInstruction.put("parts", List.of(systemPart));
        request.put("systemInstruction", systemInstruction);

        // Conversation contents
        List<Map<String, Object>> contents = new ArrayList<>();

        // Add history
        if (history != null) {
            for (ChatMessageDto msg : history) {
                Map<String, Object> content = new HashMap<>();
                content.put("role", "user".equals(msg.getRole()) ? "user" : "model");
                Map<String, String> part = new HashMap<>();
                part.put("text", msg.getContent());
                content.put("parts", List.of(part));
                contents.add(content);
            }
        }

        // Add current user message
        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        Map<String, String> userPart = new HashMap<>();
        userPart.put("text", userMessage);
        userContent.put("parts", List.of(userPart));
        contents.add(userContent);

        request.put("contents", contents);

        return request;
    }

    @SuppressWarnings("unchecked")
    private String extractReply(Map<String, Object> responseBody) {
        if (responseBody == null) {
            return "Sorry, I didn't get a response. Please try again.";
        }

        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    return (String) parts.get(0).get("text");
                }
            }
        } catch (Exception e) {
            return "Sorry, I had trouble understanding the response. Please try again.";
        }

        return "Sorry, I didn't get a valid response. Please try again.";
    }
}

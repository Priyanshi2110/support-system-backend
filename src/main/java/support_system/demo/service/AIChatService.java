package support_system.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AIChatService {

    @Value("${gemini.api.key}")
    private String API_KEY;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String getAIResponse(String message) {

        try {
            // ✅ SAFE JSON (no string.format)
            ObjectNode root = mapper.createObjectNode();
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode content = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();

            ObjectNode part = mapper.createObjectNode();
            part.put("text", "You are a supportive mental health assistant. Respond empathetically. User: " + message);

            parts.add(part);
            content.put("role", "user");
            content.set("parts", parts);
            contents.add(content);
            root.set("contents", contents);

            String jsonBody = mapper.writeValueAsString(root);

            String response = webClient.post()
                    .uri("/v1beta/models/gemini-2.0-flash:generateContent?key=" + API_KEY)
                    .header("Content-Type", "application/json")
                    .bodyValue(jsonBody)
                    .retrieve()
                    .onStatus(status -> status.isError(), res ->
                            res.bodyToMono(String.class)
                                    .map(err -> new RuntimeException("Gemini API Error: " + err))
                    )
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Gemini Response: " + response);

            JsonNode rootNode = mapper.readTree(response);

            JsonNode candidates = rootNode.path("candidates");

            if (!candidates.isArray() || candidates.size() == 0) {
                return "I'm here for you.";
            }

            JsonNode partsNode = candidates.get(0)
                    .path("content")
                    .path("parts");

            if (!partsNode.isArray() || partsNode.size() == 0) {
                return "I'm here for you.";
            }

            return partsNode.get(0)
                    .path("text")
                    .asText("I'm here for you.");

        } catch (Exception e) {
            e.printStackTrace();
            return "I'm here for you. Please try again.";
        }
    }
}
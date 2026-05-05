package support_system.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AIChatService {

    @Value("${OPENROUTER_API_KEY}")
    private String API_KEY;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://openrouter.ai/api/v1")
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String getAIResponse(String message) {
        try {
            // Build request JSON
            ObjectNode root = mapper.createObjectNode();

            // 🟢 EASIEST FREE ROUTER (avoids model 404s)
            root.put("model", "openrouter/free");

            ArrayNode messages = mapper.createArrayNode();

            ObjectNode system = mapper.createObjectNode();
            system.put("role", "system");
            system.put("content", "You are a supportive mental health assistant. Respond empathetically and safely.");

            ObjectNode user = mapper.createObjectNode();
            user.put("role", "user");
            user.put("content", message);

            messages.add(system);
            messages.add(user);

            root.set("messages", messages);

            // Call OpenRouter
            String response = webClient.post()
                    .uri("/chat/completions") // IMPORTANT: do NOT add /api/v1 again
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                    .header("HTTP-Referer", "http://localhost") // keep simple for testing
                    .header("X-Title", "MindCare AI")
                    .header("User-Agent", "Mozilla/5.0") // helps avoid 404 routing issues
                    .bodyValue(mapper.writeValueAsString(root))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("OpenRouter Response: " + response);

            if (response == null || response.isEmpty()) {
                return "I'm here for you 💙";
            }

            JsonNode rootNode = mapper.readTree(response);

            // Surface API errors if present
            if (rootNode.has("error")) {
                return rootNode.get("error").toString();
            }

            JsonNode choices = rootNode.path("choices");
            if (!choices.isArray() || choices.size() == 0) {
                return "I'm here for you 💙";
            }

            return choices.get(0)
                    .path("message")
                    .path("content")
                    .asText("I'm here for you 💙");

        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage(); // keep during debugging
        }
    }
}
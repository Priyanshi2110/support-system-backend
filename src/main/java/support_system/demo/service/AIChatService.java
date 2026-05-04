package support_system.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AIChatService {

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    private final String API_KEY = "AIzaSyDqjQvV8ruik0zaQwt9Q5WLctRhVaoo88U";

    public String getAIResponse(String message) {

    try {
        String response = webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY)
                .header("Content-Type", "application/json")
                .bodyValue("""
                {
                  "contents": [
                    {
                      "role": "user",
                      "parts": [
                        {
                          "text": "You are a supportive mental health assistant. Respond empathetically. User: %s"
                        }
                      ]
                    }
                  ]
                }
                """.formatted(message))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        System.out.println("Gemini Response: " + response);

        JsonNode root = mapper.readTree(response);

        return root
                .path("candidates")
                .get(0)
                .path("content")
                .path("parts")
                .get(0)
                .path("text")
                .asText();

    } catch (Exception e) {
        e.printStackTrace();
        return "I'm here for you. Please try again.";
    }
}
}
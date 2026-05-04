package support_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import support_system.demo.model.ChatMessage;
import support_system.demo.security.JwtUtil;
import support_system.demo.service.TherapistService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/therapist")
@CrossOrigin(origins = "*")
public class TherapistController {

    @Autowired
    private TherapistService therapistService;

    @Autowired
    private JwtUtil jwtUtil;

    // 🚨 GET DANGER CASES
    @GetMapping("/danger")
    public List<ChatMessage> getDangerChats() {
        return therapistService.getDangerMessages();
    }

    // 🚩 GET FLAGGED CHATS
    @GetMapping("/flagged")
    public List<ChatMessage> getFlaggedChats() {
        return therapistService.getFlaggedMessages();
    }

    // 👤 GET USER CHAT HISTORY FOR A THERAPIST CASE
    @GetMapping("/chat/{anonymousId}")
    public List<ChatMessage> getUserChat(@PathVariable String anonymousId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String therapistEmail = jwtUtil.extractEmail(token);
        return therapistService.getUserChat(therapistEmail, anonymousId);
    }

    // 💬 THERAPIST REPLY
    @PostMapping("/reply")
    public ChatMessage reply(@RequestBody ChatMessage msg, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String therapistEmail = jwtUtil.extractEmail(token);
        msg.setAssignedTherapistEmail(therapistEmail);
        return therapistService.sendTherapistReply(msg);
    }

    // 🔒 GET ANONYMOUS STUDENT CASES (privacy protected)
    @GetMapping("/cases")
    public List<Map<String, Object>> getAnonymousStudentCases(@RequestParam(required = false) String therapistEmail) {
        List<ChatMessage> dangerMessages;
        if (therapistEmail != null && !therapistEmail.isEmpty()) {
            dangerMessages = therapistService.getAssignedCases(therapistEmail);
        } else {
            dangerMessages = therapistService.getDangerMessages();
        }

        Map<String, List<ChatMessage>> groupedByAnonymousId = dangerMessages.stream()
                .filter(msg -> msg.getAnonymousId() != null && !msg.getAnonymousId().isEmpty())
                .collect(Collectors.groupingBy(ChatMessage::getAnonymousId));

        return groupedByAnonymousId.entrySet().stream()
                .map(entry -> {
                    String anonymousId = entry.getKey();
                    List<ChatMessage> messages = entry.getValue();
                    ChatMessage latestMessage = messages.stream()
                            .max((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                            .orElse(null);

                    Map<String, Object> caseData = new java.util.HashMap<>();
                    caseData.put("anonymousId", anonymousId);
                    caseData.put("latestMessage", latestMessage != null ? latestMessage.getMessage() : "");
                    caseData.put("timestamp", latestMessage != null ? latestMessage.getTimestamp() : null);
                    caseData.put("caseNumber", anonymousId != null ? Math.abs(anonymousId.hashCode()) % 100 + 1 : 0);
                    return caseData;
                })
                .collect(Collectors.toList());
    }
}
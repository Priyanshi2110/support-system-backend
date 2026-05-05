package support_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import support_system.demo.model.ChatMessage;
import support_system.demo.service.ChatService;
import support_system.demo.service.TherapistService;
import support_system.demo.service.AIChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private TherapistService therapistService;

    // 💬 USER → AI
    @PostMapping("/send")
public ChatMessage sendMessage(@RequestBody ChatMessage msg) {

    // ✅ 1. SAVE USER MESSAGE
    msg.setRole("USER");
    ChatMessage savedUser = chatService.saveMessage(msg);

    // ✅ 2. GET AI RESPONSE
    String aiReply = aiChatService.getAIResponse(msg.getMessage());

    // ✅ 3. CREATE NEW AI MESSAGE (IMPORTANT)
    ChatMessage aiMessage = new ChatMessage();
    aiMessage.setSenderEmail(msg.getSenderEmail());
    aiMessage.setMessage(aiReply);
    aiMessage.setRole("AI");
    aiMessage.setTimestamp(java.time.LocalDateTime.now());
    aiMessage.setAssignedTherapistEmail(null);
    aiMessage.setAnonymousId(msg.getAnonymousId());

    // ✅ 4. SAVE AI MESSAGE
    ChatMessage savedAI = chatService.saveMessage(aiMessage);

    // ✅ 5. RETURN AI (frontend + socket will use this)
    return savedAI;
}

    // 📥 STUDENT CHAT HISTORY
    @GetMapping("/{email}")
    public List<ChatMessage> getChat(@PathVariable String email) {
        return chatService.getChatByUser(email);
    }

    // 👥 STUDENT ↔ THERAPIST CHAT
    @GetMapping("/therapist/{email}/{therapistEmail}")
    public List<ChatMessage> getTherapistChat(
            @PathVariable String email,
            @PathVariable String therapistEmail
    ) {
        return chatService.getTherapistChatByEmailAndTherapistEmail(email, therapistEmail);
    }

    // 💬 STUDENT → THERAPIST
    @PostMapping("/therapist/reply")
    public ChatMessage studentReply(@RequestBody ChatMessage msg) {
        return chatService.saveStudentReply(msg);
    }

    // 💬 THERAPIST → STUDENT 🔥 (IMPORTANT)
    @PostMapping("/therapist/send")
    public ChatMessage therapistReply(@RequestBody ChatMessage msg) {
        return therapistService.sendTherapistReply(msg);
    }
}
package support_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import support_system.demo.model.ChatMessage;
import support_system.demo.service.ChatService;
import support_system.demo.service.TherapistService;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private TherapistService therapistService;

    // 💬 USER → AI
    @PostMapping("/send")
    public ChatMessage sendMessage(@RequestBody ChatMessage msg) {
        return chatService.saveMessage(msg);
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
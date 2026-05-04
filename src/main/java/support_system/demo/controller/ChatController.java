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

    // 💬 SEND MESSAGE (USER → AI FLOW)
    @PostMapping("/send")
    public ChatMessage sendMessage(@RequestBody ChatMessage msg) {
        return chatService.saveMessage(msg);
    }

    // 📥 GET CHAT HISTORY
    @GetMapping("/{email}")
    public List<ChatMessage> getChat(@PathVariable String email) {
        return chatService.getChatByUser(email);
    }

    // 👥 GET THERAPIST CONVERSATION FOR STUDENT
    @GetMapping("/therapist/{email}/{therapistEmail}")
    public List<ChatMessage> getTherapistChat(@PathVariable String email, @PathVariable String therapistEmail) {
        return chatService.getTherapistChatByEmailAndTherapistEmail(email, therapistEmail);
    }

    // 💬 STUDENT REPLY TO THERAPIST
    @PostMapping("/therapist/reply")
    public ChatMessage studentReply(@RequestBody ChatMessage msg) {
        return chatService.saveStudentReply(msg);
    }
}
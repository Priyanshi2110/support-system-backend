package support_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import support_system.demo.model.ChatMessage;
import support_system.demo.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatRepo;

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private AlertService alertService;

    // 💬 USER → AI FLOW
    public ChatMessage saveMessage(ChatMessage msg) {

        // 🚫 BLOCK therapist messages from entering AI flow
        if (msg.getAssignedTherapistEmail() != null && !msg.getAssignedTherapistEmail().isEmpty()) {
            throw new IllegalArgumentException("Use /api/chat/therapist/reply for therapist messages");
        }

        // 1️⃣ USER MESSAGE
        msg.setRole("USER");
        msg.setTimestamp(LocalDateTime.now());
        msg.setStatus("SAFE");
        msg.setFlagged(false);

        // ❌ DO NOT set assignedTherapistEmail = null here

        // Generate anonymous ID
        if (msg.getAnonymousId() == null || msg.getAnonymousId().isEmpty()) {
            msg.setAnonymousId(generateAnonymousId(msg.getSenderEmail()));
        }

        ChatMessage savedUserMsg = chatRepo.save(msg);

        // 2️⃣ CHECK DANGER
        boolean isDanger = isDangerous(msg.getMessage());

        // 3️⃣ AI RESPONSE
        String aiResponse = null;
        if (!isDanger) {
            aiResponse = aiChatService.getAIResponse(msg.getMessage());
        }

        // 4️⃣ AI MESSAGE
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderEmail(msg.getSenderEmail());
        aiMsg.setAnonymousId(msg.getAnonymousId());
        aiMsg.setTimestamp(LocalDateTime.now());
        aiMsg.setRole("AI");

        // ✅ AI messages should NOT have therapist
        aiMsg.setAssignedTherapistEmail(null);

        // 🚨 DANGER CASE
        if (isDanger) {

            aiMsg.setMessage("⚠️ We noticed you may be in distress. A therapist has been alerted and support is available.");
            aiMsg.setStatus("DANGER");

            savedUserMsg.setStatus("DANGER");
            savedUserMsg.setFlagged(true);

            alertService.createAlert(msg.getSenderEmail(), msg.getMessage());

        } else {

            aiMsg.setMessage(aiResponse);
            aiMsg.setStatus("SAFE");

            savedUserMsg.setStatus("SAFE");
            savedUserMsg.setFlagged(false);
        }

        // 5️⃣ SAVE AI MESSAGE
        chatRepo.save(aiMsg);

        // 6️⃣ UPDATE USER MESSAGE
        chatRepo.save(savedUserMsg);

        return savedUserMsg;
    }

    // 🔐 Generate anonymous ID
    private String generateAnonymousId(String email) {
        return "Student-" + Math.abs(email.hashCode()) % 10000;
    }

    // 🚨 Danger keyword detection
    private boolean isDangerous(String text) {

        if (text == null) return false;

        String lowerText = text.toLowerCase();

        String[] dangerKeywords = {
                "suicide",
                "kill myself",
                "end my life",
                "die",
                "self harm",
                "cut myself",
                "hopeless",
                "depressed",
                "no reason to live",
                "want to disappear"
        };

        for (String keyword : dangerKeywords) {
            if (lowerText.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    // 📥 FETCH AI CHAT ONLY
    public List<ChatMessage> getChatByUser(String email) {

        List<ChatMessage> messages =
                chatRepo.findBySenderEmailAndRoleInOrderByTimestampAsc(
                        email,
                        List.of("USER", "AI")
                );

        // 🔥 REMOVE therapist messages
        return messages.stream()
                .filter(msg -> msg.getAssignedTherapistEmail() == null)
                .toList();
    }

    // 👥 THERAPIST CHAT
    public List<ChatMessage> getTherapistChatByEmailAndTherapistEmail(String email, String therapistEmail) {
        return chatRepo.findTherapistConversationForStudent(email, therapistEmail);
    }

    // 💬 STUDENT → THERAPIST (NO AI HERE)
    public ChatMessage saveStudentReply(ChatMessage msg) {

        msg.setRole("USER");
        msg.setStatus("SAFE");
        msg.setFlagged(false);
        msg.setTimestamp(LocalDateTime.now());

        // ✅ MUST have therapist email
        if (msg.getAssignedTherapistEmail() == null || msg.getAssignedTherapistEmail().isEmpty()) {
            throw new IllegalArgumentException("Therapist email must be provided");
        }

        if (msg.getAnonymousId() == null || msg.getAnonymousId().isEmpty()) {
            msg.setAnonymousId(generateAnonymousId(msg.getSenderEmail()));
        }

        return chatRepo.save(msg);
    }
}
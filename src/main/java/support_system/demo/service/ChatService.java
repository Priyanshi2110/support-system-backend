package support_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import support_system.demo.model.ChatMessage;
import support_system.demo.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatRepo;

    @Autowired
    private AIChatService aiChatService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private NotificationService notificationService;

    // 💬 USER → AI FLOW
    public ChatMessage saveMessage(ChatMessage msg) {

        if (msg.getSenderEmail() == null || msg.getSenderEmail().isEmpty()) {
            throw new RuntimeException("Sender email is required");
        }

        if (msg.getMessage() == null || msg.getMessage().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        // 🚫 Prevent therapist misuse
        if (msg.getAssignedTherapistEmail() != null && !msg.getAssignedTherapistEmail().isEmpty()) {
            throw new IllegalArgumentException("Use therapist endpoint");
        }

        // USER MESSAGE
        msg.setRole("USER");
        msg.setTimestamp(LocalDateTime.now());
        msg.setStatus("SAFE");
        msg.setFlagged(false);

        if (msg.getAnonymousId() == null || msg.getAnonymousId().isEmpty()) {
            msg.setAnonymousId(generateAnonymousId(msg.getSenderEmail()));
        }

        ChatMessage savedUserMsg = chatRepo.save(msg);

        // 🔥 Notify therapists
        notificationService.notifyAllTherapists(savedUserMsg);

        boolean isDanger = isDangerous(msg.getMessage());

        String aiResponse = null;
        if (!isDanger) {
            try {
                aiResponse = aiChatService.getAIResponse(msg.getMessage());
            } catch (Exception e) {
                aiResponse = "I'm here for you.";
            }
        }

        // AI MESSAGE
        ChatMessage aiMsg = new ChatMessage();
        aiMsg.setSenderEmail(msg.getSenderEmail());
        aiMsg.setAnonymousId(msg.getAnonymousId());
        aiMsg.setTimestamp(LocalDateTime.now());
        aiMsg.setRole("AI");
        aiMsg.setAssignedTherapistEmail(null);
        aiMsg.setFlagged(false);
        aiMsg.setStatus("SAFE");

        if (isDanger) {
            aiMsg.setMessage("⚠️ We noticed you may be in distress. A therapist has been alerted.");
            aiMsg.setStatus("DANGER");

            savedUserMsg.setStatus("DANGER");
            savedUserMsg.setFlagged(true);

            alertService.createAlert(msg.getSenderEmail(), msg.getMessage());
        } else {
            aiMsg.setMessage(aiResponse != null ? aiResponse : "I'm here for you.");
        }

        chatRepo.save(aiMsg);

        // 🔔 Notify student
        notificationService.notifyStudent(msg.getSenderEmail(), aiMsg);

        return savedUserMsg;
    }

    // 🔐 Anonymous ID
    private String generateAnonymousId(String email) {
        if (email == null) return "Student-0000";
        return "Student-" + Math.abs(email.hashCode()) % 10000;
    }

    // 🚨 Danger detection
    private boolean isDangerous(String text) {
        if (text == null) return false;

        String lowerText = text.toLowerCase();

        String[] dangerKeywords = {
                "suicide", "kill myself", "end my life",
                "die", "self harm", "cut myself",
                "hopeless", "depressed", "no reason to live"
        };

        for (String keyword : dangerKeywords) {
            if (lowerText.contains(keyword)) return true;
        }

        return false;
    }

    // 📥 USER CHAT (FIXED 🔥)
    public List<ChatMessage> getChatByUser(String email) {

        return chatRepo.findBySenderEmailOrderByTimestampAsc(email)
                .stream()
                .filter(msg ->
                        msg.getAssignedTherapistEmail() == null &&  // 🔥 ONLY AI CHAT
                        ("USER".equals(msg.getRole()) || "AI".equals(msg.getRole()))
                )
                .collect(Collectors.toList());
    }

    // 👥 THERAPIST CHAT
    public List<ChatMessage> getTherapistChatByEmailAndTherapistEmail(String email, String therapistEmail) {
        return chatRepo.findTherapistConversationForStudent(email, therapistEmail);
    }

    // 💬 STUDENT → THERAPIST
    public ChatMessage saveStudentReply(ChatMessage msg) {

        if (msg.getSenderEmail() == null || msg.getSenderEmail().isEmpty()) {
            throw new RuntimeException("Sender email is required");
        }

        if (msg.getAssignedTherapistEmail() == null || msg.getAssignedTherapistEmail().isEmpty()) {
            throw new IllegalArgumentException("Therapist email must be provided");
        }

        if (msg.getMessage() == null || msg.getMessage().isEmpty()) {
            throw new RuntimeException("Message cannot be empty");
        }

        msg.setRole("USER");
        msg.setStatus("SAFE");
        msg.setFlagged(false);
        msg.setTimestamp(LocalDateTime.now());

        if (msg.getAnonymousId() == null || msg.getAnonymousId().isEmpty()) {
            msg.setAnonymousId(generateAnonymousId(msg.getSenderEmail()));
        }

        ChatMessage saved = chatRepo.save(msg);

        // 🔔 Notify therapist
        notificationService.notifyTherapist(
                msg.getAssignedTherapistEmail(),
                saved
        );

        return saved;
    }
}
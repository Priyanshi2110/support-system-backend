package support_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import support_system.demo.model.ChatMessage;
import support_system.demo.repository.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TherapistService {

    @Autowired
    private ChatMessageRepository chatRepo;

    @Autowired
    private NotificationService notificationService;

    // 🚨 Get all danger messages
    public List<ChatMessage> getDangerMessages() {
        return chatRepo.findByStatus("DANGER");
    }

    // 🚩 Get all flagged messages
    public List<ChatMessage> getFlaggedMessages() {
        return chatRepo.findByFlaggedTrue();
    }

    // 👤 Get conversation (therapist + student)
    public List<ChatMessage> getUserChat(String therapistEmail, String anonymousId) {
        return chatRepo.findTherapistCaseConversation(anonymousId, therapistEmail);
    }

    // 💬 Therapist reply
    public ChatMessage sendTherapistReply(ChatMessage msg) {

        // ✅ VALIDATION (no logic change, just safer)
        if (msg.getAssignedTherapistEmail() == null || msg.getAssignedTherapistEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Therapist email is required");
        }

        if (msg.getSenderEmail() == null || msg.getSenderEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Student email is required");
        }

        if (msg.getMessage() == null || msg.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        // 🔥 CRITICAL FIX (this was missing → causing 500)
        if (msg.getAnonymousId() == null || msg.getAnonymousId().trim().isEmpty()) {
            msg.setAnonymousId("Student-0000"); // fallback instead of crash
        }

        // ✅ SET FIELDS
        msg.setRole("THERAPIST");
        msg.setStatus("SAFE");
        msg.setFlagged(false);
        msg.setTimestamp(LocalDateTime.now());

        // 💾 SAVE MESSAGE
        ChatMessage saved = chatRepo.save(msg);

        // 🔔 SEND TO STUDENT
        notificationService.notifyStudent(
                saved.getSenderEmail(),
                saved
        );

        return saved;
    }

    // 📂 Assigned cases for therapist
    public List<ChatMessage> getAssignedCases(String therapistEmail) {
        return chatRepo.findByAssignedTherapistEmailOrderByTimestampAsc(therapistEmail);
    }
}
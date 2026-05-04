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

    // 🚨 Get all danger messages
    public List<ChatMessage> getDangerMessages() {
        return chatRepo.findByStatus("DANGER");
    }

    // 🚩 Get all flagged messages
    public List<ChatMessage> getFlaggedMessages() {
        return chatRepo.findByFlaggedTrue();
    }

    // 👤 Get the therapist conversation for a high-alert student (by anonymous ID and therapist)
    public List<ChatMessage> getUserChat(String therapistEmail, String anonymousId) {
        return chatRepo.findTherapistCaseConversation(anonymousId, therapistEmail);
    }

    // 💬 Therapist reply (by anonymous ID)
    public ChatMessage sendTherapistReply(ChatMessage msg) {
        msg.setRole("THERAPIST");
        msg.setStatus("SAFE");
        msg.setFlagged(false);
        msg.setTimestamp(LocalDateTime.now());
        if (msg.getAssignedTherapistEmail() == null || msg.getAssignedTherapistEmail().isEmpty()) {
            throw new IllegalArgumentException("Assigned therapist email is required for therapist replies.");
        }
        if (msg.getAnonymousId() == null || msg.getAnonymousId().isEmpty()) {
            throw new IllegalArgumentException("Anonymous ID is required for therapist replies.");
        }
        return chatRepo.save(msg);
    }

    public List<ChatMessage> getAssignedCases(String therapistEmail) {
        return chatRepo.findByAssignedTherapistEmailOrderByTimestampAsc(therapistEmail);
    }
}

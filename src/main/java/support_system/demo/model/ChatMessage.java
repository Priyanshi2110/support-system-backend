package support_system.demo.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderEmail; // student email

    private String anonymousId; // anonymous identifier

    private String assignedTherapistEmail; // therapist assignment

    // ✅ FIXED (VERY IMPORTANT)
    @Column(columnDefinition = "TEXT")
    private String message;

    private String role; // USER / AI / THERAPIST

    private String status; // SAFE / DANGER / ESCALATED

    private LocalDateTime timestamp;

    private boolean flagged = false;

    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getAnonymousId() {
        return anonymousId;
    }

    public void setAnonymousId(String anonymousId) {
        this.anonymousId = anonymousId;
    }

    public String getAssignedTherapistEmail() {
        return assignedTherapistEmail;
    }

    public void setAssignedTherapistEmail(String assignedTherapistEmail) {
        this.assignedTherapistEmail = assignedTherapistEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }
}
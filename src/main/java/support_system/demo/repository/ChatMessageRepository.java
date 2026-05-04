package support_system.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import support_system.demo.model.ChatMessage;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 👤 Get chat by student email
    List<ChatMessage> findBySenderEmail(String email);

    // � Get only student / AI chat messages
    List<ChatMessage> findBySenderEmailAndRoleInOrderByTimestampAsc(String email, List<String> roles);

    // �🚨 Get messages by status (SAFE / DANGER / ESCALATED)
    List<ChatMessage> findByStatus(String status);

    // 🚩 Get only flagged messages
    List<ChatMessage> findByFlaggedTrue();

    @Query("SELECT c FROM ChatMessage c WHERE c.senderEmail = :email AND (c.status = 'DANGER' OR c.role = 'THERAPIST' OR c.role = 'USER') ORDER BY c.timestamp ASC")
    List<ChatMessage> findDangerAndTherapistConversation(@Param("email") String email);

    @Query("SELECT c FROM ChatMessage c WHERE c.anonymousId = :anonymousId AND (c.status = 'DANGER' OR c.role = 'THERAPIST' OR c.role = 'USER') ORDER BY c.timestamp ASC")
    List<ChatMessage> findDangerAndTherapistConversationByAnonymousId(@Param("anonymousId") String anonymousId);

    List<ChatMessage> findBySenderEmailAndAssignedTherapistEmailOrderByTimestampAsc(String senderEmail, String assignedTherapistEmail);

    List<ChatMessage> findByAssignedTherapistEmailAndAnonymousIdOrderByTimestampAsc(String assignedTherapistEmail, String anonymousId);

    List<ChatMessage> findByAssignedTherapistEmailOrderByTimestampAsc(String assignedTherapistEmail);

    @Query("SELECT c FROM ChatMessage c WHERE c.anonymousId = :anonymousId AND (c.flagged = true OR c.assignedTherapistEmail = :therapistEmail) ORDER BY c.timestamp ASC")
    List<ChatMessage> findTherapistCaseConversation(@Param("anonymousId") String anonymousId, @Param("therapistEmail") String therapistEmail);

    @Query("SELECT c FROM ChatMessage c WHERE (c.senderEmail = :senderEmail OR (c.role = 'THERAPIST' AND c.assignedTherapistEmail = :therapistEmail)) AND (c.flagged = true OR c.assignedTherapistEmail = :therapistEmail) ORDER BY c.timestamp ASC")
    List<ChatMessage> findTherapistConversationForStudent(@Param("senderEmail") String senderEmail, @Param("therapistEmail") String therapistEmail);

    @Query("SELECT COUNT(DISTINCT c.senderEmail) FROM ChatMessage c")
    long countDistinctSenderEmail();

    @Query("SELECT COUNT(DISTINCT c.senderEmail) FROM ChatMessage c WHERE c.timestamp >= :since")
    long countDistinctSenderEmailByTimestampAfter(@Param("since") LocalDateTime since);
}
package support_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import support_system.demo.model.ChatMessage;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 🔔 Send to one student
    public void notifyStudent(String email, ChatMessage msg) {
        messagingTemplate.convertAndSend("/topic/chat/" + email, msg);
    }

    // 🔔 Send to one therapist
    public void notifyTherapist(String email, ChatMessage msg) {
        messagingTemplate.convertAndSend("/topic/therapist/" + email, msg);
    }

    // 🔥 NEW → Send to ALL therapists
    public void notifyAllTherapists(ChatMessage msg) {
        messagingTemplate.convertAndSend("/topic/therapists", msg);
    }
}
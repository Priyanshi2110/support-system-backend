package support_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import support_system.demo.repository.AlertRepository;
import support_system.demo.repository.ChatMessageRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private ChatMessageRepository chatRepo;

    @Autowired
    private AlertRepository alertRepo;

    @GetMapping("/student")
    public Map<String, Object> getStudentStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChats", chatRepo.count());
        stats.put("activeUsers", chatRepo.countDistinctSenderEmail());
        stats.put("alertsToday", alertRepo.countByCreatedAtBetween(startOfToday, endOfToday));
        stats.put("activeUsersToday", chatRepo.countDistinctSenderEmailByTimestampAfter(startOfToday));
        return stats;
    }

    @GetMapping("/therapist")
    public Map<String, Object> getTherapistStats() {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPatients", chatRepo.countDistinctSenderEmail());
        stats.put("activeAlerts", alertRepo.countByResolvedFalse());
        stats.put("totalChats", chatRepo.count());
        stats.put("resolvedCases", alertRepo.countByResolvedTrue());
        stats.put("alertsToday", alertRepo.countByCreatedAtBetween(startOfToday, endOfToday));
        return stats;
    }
}

package support_system.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import support_system.demo.model.Alert;
import support_system.demo.repository.AlertRepository;

import java.util.List;

@Service
public class AlertService {

    @Autowired
    private AlertRepository alertRepo;

    // ✅ Create alert
    public Alert createAlert(String email, String message) {
        Alert alert = new Alert();
        alert.setUserEmail(email);
        alert.setMessage(message);
        alert.setSeverity("HIGH");
        alert.setResolved(false);

        return alertRepo.save(alert);
    }

    // ✅ Get active alerts (for therapist)
    public List<Alert> getActiveAlerts() {
        return alertRepo.findByResolvedFalse();
    }

    // ❗ ADD THIS (missing method causing red line)
    public List<Alert> getAllAlerts() {
        return alertRepo.findAll();
    }

    // ❗ ADD THIS (missing method causing red line)
    public List<Alert> getAlertsByUserEmail(String email) {
        return alertRepo.findByUserEmail(email);
    }

    // ✅ Resolve alert
    public Alert resolveAlert(Long id) {
        Alert alert = alertRepo.findById(id).orElseThrow();
        alert.setResolved(true);
        return alertRepo.save(alert);
    }
}
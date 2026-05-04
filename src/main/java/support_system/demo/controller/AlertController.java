package support_system.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import support_system.demo.model.Alert;
import support_system.demo.service.AlertService;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "http://localhost:3003")
public class AlertController {

    @Autowired
    private AlertService alertService;

    // ✅ GET ALL ACTIVE ALERTS
    @GetMapping
    public List<Alert> getActiveAlerts() {
        return alertService.getActiveAlerts();
    }

    // ✅ CREATE ALERT
    @PostMapping
    public Alert createAlert(@RequestBody Alert alert) {
        return alertService.createAlert(
                alert.getUserEmail(),
                alert.getMessage()
        );
    }

    // ✅ RESOLVE ALERT
    @PutMapping("/resolve/{id}")
    public Alert resolveAlert(@PathVariable Long id) {
        return alertService.resolveAlert(id);
    }
}
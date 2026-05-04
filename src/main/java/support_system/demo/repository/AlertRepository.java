package support_system.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import support_system.demo.model.Alert;
import java.time.LocalDateTime;
import java.util.List;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    List<Alert> findByResolvedFalse();

    List<Alert> findByResolvedTrue();

    long countByResolvedFalse();

    long countByResolvedTrue();

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Alert> findByUserEmail(String email);
}
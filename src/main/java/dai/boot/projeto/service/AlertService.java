package dai.boot.projeto.service;

import dai.boot.projeto.entities.Alert;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface AlertService {
    Alert createAlert(Alert alert);
    List<Alert> getActiveAlerts();
    Alert resolveAlert(Long id, String resolverUsername);
    Optional<Alert> getAlert(Long id);
    Alert markAsInconsistent(Long id, String username);
    Map<String, Long> getSeverityStats();
}
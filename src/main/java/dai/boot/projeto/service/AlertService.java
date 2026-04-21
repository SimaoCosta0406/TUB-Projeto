package dai.boot.projeto.service;

import dai.boot.projeto.entities.Alert;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AlertService {
    Alert createAlert(Alert alert);

    // Todos os alertas ativos (status = ACTIVE) — para o supervisor ver
    List<Alert> getActiveAlerts();

    // Alertas aceites pelo supervisor (status = ACCEPTED) — para o admin ver
    List<Alert> getAcceptedAlerts();

    // Alertas devolvidos pelo admin (status = PENDING) — supervisor vê motivo e volta a aceitar
    List<Alert> getPendingAlerts();

    Optional<Alert> getAlert(Long id);

    // Supervisor aceita um alerta
    Alert acceptAlert(Long id, String supervisorUsername);

    // Admin resolve um alerta
    Alert resolveAlert(Long id, String adminUsername);

    // Admin marca como inconsistente com motivo — volta para PENDING
    Alert markAsInconsistent(Long id, String adminUsername, String reason);

    // Mantido para compatibilidade — chama o de cima sem motivo
    Alert markAsInconsistent(Long id, String username);

    Map<String, Long> getSeverityStats();
}
package dai.boot.projeto.service;

import dai.boot.projeto.entities.Alert;
import dai.boot.projeto.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Override
    public Alert createAlert(Alert alert) {
        List<Alert> existingActive = alertRepository.findByTypeAndStatus(alert.getType(), "ACTIVE");

        for (Alert existing : existingActive) {
            if (existing.getSource() != null && existing.getSource().equals(alert.getSource())) {
                return existing;
            }
        }

        return alertRepository.save(alert);
    }

    @Override
    public List<Alert> getActiveAlerts() {
        return alertRepository.findByStatus("ACTIVE");
    }

    @Override
    public Optional<Alert> getAlert(Long id) {
        return alertRepository.findById(id);
    }

    @Override
    public Alert resolveAlert(Long id, String resolverUsername) {
        Optional<Alert> optionalAlert = alertRepository.findById(id);

        if (optionalAlert.isPresent()) {
            Alert alert = optionalAlert.get();
            
            // LOG DE TESTE: Adiciona esta linha para veres no terminal se o código chega aqui
            System.out.println("A resolver alerta " + id + " por " + resolverUsername);

            alert.setStatus("RESOLVED");
            alert.setResolvedAt(LocalDateTime.now());
            
            // Se não fizeres o save(), a alteração fica só na memória e não vai para a BD
            return alertRepository.save(alert); 
        }
        return null;
    }

    @Override
    public Alert markAsInconsistent(Long id, String reason) {
        Optional<Alert> optionalAlert = alertRepository.findById(id);
        if (optionalAlert.isPresent()) {
            Alert alert = optionalAlert.get();
            alert.setStatus("INCONSISTENT");        alert.setMetadata(alert.getMetadata() + " | Inconsistency Reason: " + reason);
            return alertRepository.save(alert);
        }
        return null;
    }

    @Override
    public Map<String, Long> getSeverityStats() {
        Map<String, Long> stats = new HashMap<>();
        // Filtramos apenas pelos "ACTIVE" para o supervisor saber o que tem pendente
        stats.put("HIGH", alertRepository.countBySeverityAndStatus("HIGH", "ACTIVE"));
        stats.put("MEDIUM", alertRepository.countBySeverityAndStatus("MEDIUM", "ACTIVE"));
        stats.put("LOW", alertRepository.countBySeverityAndStatus("LOW", "ACTIVE"));
        return stats;
    }

    private Alert updateStatus(Long id, String newStatus, String username) {
        Optional<Alert> opt = alertRepository.findById(id);
        if (opt.isPresent()) {
            Alert a = opt.get();
            a.setStatus(newStatus);
            a.setResolvedAt(LocalDateTime.now());
            a.setMetadata(a.getMetadata() + " | Action by: " + username);
            return alertRepository.save(a);
        }
        return null;

    }
}
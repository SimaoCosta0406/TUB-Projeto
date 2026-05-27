package dai.boot.projeto.service;

import dai.boot.projeto.entities.Alert;
import dai.boot.projeto.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private AlertRepository alertRepository;

    @Override
    public Alert createAlert(Alert alert) {
        if (alert.getStatus() == null) {
            alert.setStatus("ACTIVE");
        }
        return alertRepository.save(alert);
    }

    // Alertas novos — visíveis para o Supervisor aceitar
    @Override
    public List<Alert> getActiveAlerts() {
        return alertRepository.findAll().stream()
                .filter(a -> "ACTIVE".equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    // Alertas aceites pelo Supervisor — visíveis para o Admin
    @Override
    public List<Alert> getAcceptedAlerts() {
        return alertRepository.findAll().stream()
                .filter(a -> "ACCEPTED".equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    // Alertas devolvidos pelo Admin como inconsistentes — Supervisor vê motivo
    @Override
    public List<Alert> getPendingAlerts() {
        return alertRepository.findAll().stream()
                .filter(a -> "PENDING".equals(a.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Alert> getAlert(Long id) {
        return alertRepository.findById(id);
    }

    // Supervisor aceita um alerta: ACTIVE ou PENDING → ACCEPTED
    @Override
    public Alert acceptAlert(Long id, String supervisorUsername) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado: " + id));

        alert.setStatus("ACCEPTED");
        alert.setAcceptedBy(supervisorUsername);
        alert.setInconsistentReason(null); // limpa motivo anterior se existia
        return alertRepository.save(alert);
    }

    // Admin resolve: ACCEPTED → RESOLVED
    @Override
    public Alert resolveAlert(Long id, String adminUsername) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado: " + id));

        alert.setStatus("RESOLVED");
        alert.setResolvedAt(LocalDateTime.now());
        return alertRepository.save(alert);
    }

    // Admin marca como inconsistente com motivo: ACCEPTED → PENDING
    @Override
    public Alert markAsInconsistent(Long id, String adminUsername, String reason) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado: " + id));

        alert.setStatus("PENDING");
        alert.setInconsistentReason(reason);
        alert.setAcceptedBy(null); // volta a requerer aceitação do supervisor
        return alertRepository.save(alert);
    }

    // Compatibilidade com interface antiga (sem motivo)
    @Override
    public Alert markAsInconsistent(Long id, String username) {
        return markAsInconsistent(id, username, "Sem motivo especificado.");
    }

    @Override
    public Map<String, Long> getSeverityStats() {
        return alertRepository.findAll().stream()
                .filter(a -> !"RESOLVED".equals(a.getStatus()))
                .collect(Collectors.groupingBy(
                        a -> a.getSeverity() != null ? a.getSeverity() : "UNKNOWN",
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Object> getAlertOptions() {
        Map<String, Object> opcoes = new java.util.HashMap<>();
        
        // Tipos de alarmes
        opcoes.put("tipos", java.util.Arrays.asList(
                "Acidente",
                "Avaria de Veículo",
                "Tráfego Congestionado",
                "Segurança",
                "Manutenção",
                "Passageiros",
                "Outro"
        ));

        // Gravidades
        opcoes.put("gravidades", java.util.Arrays.asList(
                "LOW",
                "MEDIUM",
                "HIGH"
        ));

        // Estados
        opcoes.put("estados", java.util.Arrays.asList(
                "ACTIVE",
                "PENDING",
                "ACCEPTED",
                "RESOLVED"
        ));

        // Ações recomendadas
        opcoes.put("acoes", java.util.Arrays.asList(
                "Parar Veículo",
                "Deslocar Supervisor",
                "Contactar Polícia",
                "Contactar Bombeiros",
                "Contactar Ambulância",
                "Comunicar a Passageiros",
                "Reparação no Local",
                "Encaminhar para Garagem",
                "Aumentar Frequência",
                "Reduzir Frequência",
                "Desviar Rota",
                "Sem Ação Imediata"
        ));

        return opcoes;
    }

    @Override
    public Alert updateAlertStateAndActions(Long id, String newStatus, String recommendedActionsJson, String supervisorUsername) {
        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alerta não encontrado: " + id));

        alert.setStatus(newStatus);
        alert.setRecommendedActions(recommendedActionsJson);
        if ("ACCEPTED".equalsIgnoreCase(newStatus)) {
            alert.setAcceptedBy(supervisorUsername);
            alert.setInconsistentReason(null);
        } else if ("PENDING".equalsIgnoreCase(newStatus)) {
            alert.setAcceptedBy(null);
        }
        return alertRepository.save(alert);
    }

    @Override
    public List<Alert> getWorkerAlerts() {
        return alertRepository.findAll().stream()
                .filter(a -> "ACCEPTED".equals(a.getStatus()))
                .collect(Collectors.toList());
    }
}
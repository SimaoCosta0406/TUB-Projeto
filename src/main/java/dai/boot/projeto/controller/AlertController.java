package dai.boot.projeto.controller;

import dai.boot.projeto.entities.Alert;
import dai.boot.projeto.service.AlertService;
import dai.boot.projeto.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertRepository alertRepository;

    // Alertas ACTIVE — Supervisor vê para aceitar
    @GetMapping("/active")
    public List<Alert> getActive() {
        return alertService.getActiveAlerts();
    }

    // Alertas ACCEPTED — Admin vê para resolver ou marcar inconsistente
    @GetMapping("/accepted")
    public List<Alert> getAccepted() {
        return alertService.getAcceptedAlerts();
    }

    // Alertas PENDING (devolvidos pelo admin) — Supervisor vê motivo e volta a aceitar
    @GetMapping("/pending")
    public List<Alert> getPending() {
        return alertService.getPendingAlerts();
    }

    // Rota geral — devolve todos (usado para histórico e stats)
    @GetMapping
    public List<Alert> getAll() {
        return alertRepository.findAll();
    }

    // Criar alerta
    @PostMapping
    public Alert create(@RequestBody Alert alert) {
        return alertService.createAlert(alert);
    }

    // Supervisor aceita um alerta
    @PostMapping("/{id}/accept")
    public Alert accept(@PathVariable Long id, @RequestParam String username) {
        return alertService.acceptAlert(id, username);
    }

    // Admin resolve um alerta
    @PostMapping("/{id}/resolve")
    public Alert resolve(@PathVariable Long id, @RequestParam String username) {
        return alertService.resolveAlert(id, username);
    }

    // Admin marca como inconsistente com motivo
    @PostMapping("/{id}/inconsistent")
    public Alert setInconsistent(
            @PathVariable Long id,
            @RequestParam String username,
            @RequestParam(required = false, defaultValue = "") String reason) {
        return alertService.markAsInconsistent(id, username, reason);
    }

    // Estatísticas por severidade (apenas não resolvidos)
    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return alertService.getSeverityStats();
    }

    // Histórico completo
    @GetMapping("/history")
    public List<Alert> getHistory() {
        return alertRepository.findAll();
    }
}
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

    // Listar todos ou filtrar por status (Ex: /api/alerts?status=ACTIVE)
    @GetMapping
    public List<Alert> getAll() {
        return alertService.getActiveAlerts();
    }

    // Listar apenas ativos (Rota específica pedida pelo PM)
    @GetMapping("/active")
    public List<Alert> getActive() {
        return alertService.getActiveAlerts();
    }

    // Criar alerta
    @PostMapping
    public Alert create(@RequestBody Alert alert) {
        return alertService.createAlert(alert);
    }

    // Resolver alerta
    @PostMapping("/{id}/resolve")
    public Alert resolve(@PathVariable Long id, @RequestParam String username) {
        return alertService.resolveAlert(id, username);
    }

    @PostMapping("/{id}/inconsistent")
    public Alert setInconsistent(@PathVariable Long id, @RequestParam String username) {
        // Precisas de adicionar este método à interface AlertService primeiro
        return alertService.markAsInconsistent(id, username);
    }

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        return alertService.getSeverityStats();
    }

    @GetMapping("/history")
    public List<Alert> getHistory() {
        // Chama o findAll() do repositório para trazer a tabela inteira
        return alertRepository.findAll(); 
    }
}
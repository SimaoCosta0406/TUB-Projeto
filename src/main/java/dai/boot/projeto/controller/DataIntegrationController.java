package dai.boot.projeto.controller;

import dai.boot.projeto.service.DataIngestionService;
import dai.boot.projeto.service.DataFlowMonitoringService;
import dai.boot.projeto.service.DataValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 5.1 - 5.3: Data Integration & Monitoring APIs
 */
@RestController
@RequestMapping("/api/data-integration")
@CrossOrigin(origins = "*")
public class DataIntegrationController {

    @Autowired private DataIngestionService dataIngestionService;
    @Autowired private DataFlowMonitoringService dataFlowMonitoringService;
    @Autowired private DataValidationService dataValidationService;

    // ── 5.1 PIPELINES DE INGESTÃO ────────────────────────

    /** Estatísticas dos pipelines + resultado da última execução */
    @GetMapping("/ingestion/stats")
    public Map<String, Object> getIngestionStats() {
        return dataIngestionService.getIngestionStats();
    }

    /** Executar pipeline de ocupação de passageiros manualmente */
    @PostMapping("/ingestion/run/occupancy")
    public Map<String, Object> runOccupancyPipeline() {
        return dataIngestionService.runOccupancyPipeline();
    }

    /** Executar pipeline de alertas manualmente */
    @PostMapping("/ingestion/run/alerts")
    public Map<String, Object> runAlertsPipeline() {
        return dataIngestionService.runAlertsPipeline();
    }

    /** Executar pipeline de métricas de rotas manualmente */
    @PostMapping("/ingestion/run/route-metrics")
    public Map<String, Object> runRouteMetricsPipeline() {
        return dataIngestionService.runRouteMetricsPipeline();
    }

    /** Executar todos os pipelines de uma vez */
    @PostMapping("/ingestion/run/all")
    public Map<String, Object> runAllPipelines() {
        Map<String, Object> result = new HashMap<>();
        result.put("occupancy",    dataIngestionService.runOccupancyPipeline());
        result.put("alerts",       dataIngestionService.runAlertsPipeline());
        result.put("routeMetrics", dataIngestionService.runRouteMetricsPipeline());
        return result;
    }

    // ── 5.2 MONITORIZAÇÃO DE FLUXOS ──────────────────────

    @GetMapping("/monitoring/status")
    public Map<String, Object> getDataFlowStatus() {
        return dataFlowMonitoringService.getDataFlowStatus();
    }

    @GetMapping("/monitoring/events")
    public Map<String, Object> getRecentEvents(@RequestParam(defaultValue = "30") int minutes) {
        return dataFlowMonitoringService.getRecentEvents(minutes);
    }

    @GetMapping("/monitoring/anomalies")
    public Map<String, Object> detectAnomalies() {
        return dataFlowMonitoringService.detectAnomalies();
    }

    // ── 5.3 VALIDAÇÃO DE INTEGRAÇÃO ──────────────────────

    @GetMapping("/validation/check")
    public Map<String, Object> validateIntegration() {
        return dataValidationService.validateIntegration();
    }

    // ── DASHBOARD CONSOLIDADO ─────────────────────────────

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("ingestion",  dataIngestionService.getIngestionStats());
        dashboard.put("monitoring", dataFlowMonitoringService.getDataFlowStatus());
        dashboard.put("anomalies",  dataFlowMonitoringService.detectAnomalies());
        dashboard.put("validation", dataValidationService.validateIntegration());
        dashboard.put("timestamp",  java.time.LocalDateTime.now());
        return dashboard;
    }

    @GetMapping("/health")
    public Map<String, Object> getHealth() {
        Map<String, Object> health = new HashMap<>();
        try {
            dataIngestionService.getIngestionStats();
            dataFlowMonitoringService.getDataFlowStatus();
            dataValidationService.validateIntegration();
            health.put("status", "UP");
            health.put("message", "Todos os serviços funcionando normalmente");
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("message", "Erro nos serviços: " + e.getMessage());
        }
        health.put("timestamp", java.time.LocalDateTime.now());
        return health;
    }
}
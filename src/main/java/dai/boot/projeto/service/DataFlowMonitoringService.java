package dai.boot.projeto.service;

import dai.boot.projeto.repository.AlertRepository;
import dai.boot.projeto.repository.PassengerCountRepository;
import dai.boot.projeto.repository.VehicleRepository;
import dai.boot.projeto.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 5.2 - Monitorizar Fluxos de Dados
 * Monitora a saúde e o fluxo contínuo de dados através do sistema
 */
@Service
public class DataFlowMonitoringService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataFlowMonitoringService.class);

    @Autowired
    private PassengerCountRepository passengerCountRepository;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Autowired
    private RouteRepository routeRepository;

    /**
     * Obter estado geral do fluxo de dados
     */
    public Map<String, Object> getDataFlowStatus() {
        logger.info("[5.2] Consultando estado do fluxo de dados...");
        
        Map<String, Object> status = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. Volume de dados por tipo
        Map<String, Long> dataVolume = new HashMap<>();
        dataVolume.put("passengerCounts", passengerCountRepository.count());
        dataVolume.put("alerts", alertRepository.count());
        dataVolume.put("vehicles", vehicleRepository.count());
        dataVolume.put("routes", routeRepository.count());
        status.put("dataVolume", dataVolume);

        // 2. Alertas por status
        Map<String, Long> alertsByStatus = new HashMap<>();
        alertsByStatus.put("ACTIVE", alertRepository.countByStatus("ACTIVE"));
        alertsByStatus.put("ACCEPTED", alertRepository.countByStatus("ACCEPTED"));
        alertsByStatus.put("RESOLVED", alertRepository.countByStatus("RESOLVED"));
        status.put("alertsByStatus", alertsByStatus);

        // 3. Veículos em serviço
        long vehiclesInService = vehicleRepository.countByStatus("IN_SERVICE");
        status.put("vehiclesInService", vehiclesInService);

        // 4. Rotas ativas
        long activeRoutes = routeRepository.countByStatus("ACTIVE");
        status.put("activeRoutes", activeRoutes);

        // 5. Qualidade do fluxo (score 0-100)
        int qualityScore = calculateFlowQuality();
        status.put("flowQuality", qualityScore);
        status.put("flowQualityStatus", qualityScore >= 80 ? "EXCELLENT" : 
                                        qualityScore >= 60 ? "GOOD" : 
                                        qualityScore >= 40 ? "FAIR" : "POOR");

        // 6. Timestamp do check
        status.put("checkedAt", now);
        status.put("monitoringStatus", "ACTIVE");

        logger.info("[5.2] Estado do fluxo: {} alertas ativos, {} veículos em serviço, {} rotas ativas",
            alertsByStatus.get("ACTIVE"), vehiclesInService, activeRoutes);

        return status;
    }

    /**
     * Calcular score de qualidade do fluxo (0-100)
     */
    private int calculateFlowQuality() {
        long totalAlerts = alertRepository.count();
        long resolvedAlerts = alertRepository.countByStatus("RESOLVED");
        
        long totalVehicles = vehicleRepository.count();
        long vehiclesInService = vehicleRepository.countByStatus("IN_SERVICE");

        // Score baseado em: taxa de resolução de alertas + disponibilidade de veículos
        int alertScore = totalAlerts > 0 ? (int)(resolvedAlerts * 50 / totalAlerts) : 50;
        int vehicleScore = totalVehicles > 0 ? (int)(vehiclesInService * 50 / totalVehicles) : 50;

        return Math.min(100, alertScore + vehicleScore);
    }

    /**
     * Obter histórico recente de eventos
     */
    public Map<String, Object> getRecentEvents(int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        
        Map<String, Object> events = new HashMap<>();

        // Alertas recentes
        long recentAlerts = alertRepository.countByCreatedAtAfter(since);
        events.put("recentAlerts", recentAlerts);

        // Ocupação monitorizada
        long recentOccupancy = passengerCountRepository.countByTimestampAfter(since);
        events.put("recentOccupancyRecords", recentOccupancy);

        // Período analisado
        events.put("analysisPeriodMinutes", minutes);
        events.put("checkedAt", LocalDateTime.now());

        return events;
    }

    /**
     * Detectar anomalias no fluxo
     */
    public Map<String, Object> detectAnomalies() {
        logger.info("[5.2] Detectando anomalias no fluxo...");
        
        Map<String, Object> anomalies = new HashMap<>();

        // 1. Muitos alertas ativos
        long activeAlerts = alertRepository.countByStatus("ACTIVE");
        
        boolean tooManyAlerts = activeAlerts > 5;
        anomalies.put("excessiveAlerts", tooManyAlerts);
        anomalies.put("activeAlertCount", activeAlerts);

        // 2. Poucos veículos em serviço
        long vehiclesInService = vehicleRepository.countByStatus("IN_SERVICE");
        long totalVehicles = vehicleRepository.count();
        
        boolean lowAvailability = vehiclesInService < (totalVehicles * 0.5);
        anomalies.put("lowVehicleAvailability", lowAvailability);
        anomalies.put("vehiclesInService", vehiclesInService);
        anomalies.put("totalVehicles", totalVehicles);

        // 3. Rotas inativas
        long inactiveRoutes = routeRepository.countByStatus("INACTIVE");
        
        boolean hasInactiveRoutes = inactiveRoutes > 0;
        anomalies.put("hasInactiveRoutes", hasInactiveRoutes);
        anomalies.put("inactiveRoutesCount", inactiveRoutes);

        // Status geral
        boolean anomalyDetected = tooManyAlerts || lowAvailability || hasInactiveRoutes;
        anomalies.put("anomalyDetected", anomalyDetected);
        anomalies.put("severity", anomalyDetected ? "HIGH" : "NORMAL");
        anomalies.put("checkedAt", LocalDateTime.now());

        if (anomalyDetected) {
            logger.warn("[5.2] Anomalias detectadas: Alertas={}. Veículos={}. Rotas={}",
                tooManyAlerts, lowAvailability, hasInactiveRoutes);
        }

        return anomalies;
    }
}

package dai.boot.projeto.service;

import dai.boot.projeto.entities.Alert;
import dai.boot.projeto.entities.PassengerCount;
import dai.boot.projeto.entities.Route;
import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.repository.AlertRepository;
import dai.boot.projeto.repository.PassengerCountRepository;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.repository.StopRepository;
import dai.boot.projeto.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 5.1 - Executar Pipelines de Ingestão
 */
@Service
public class DataIngestionService {

    private static final Logger logger = LoggerFactory.getLogger(DataIngestionService.class);
    private static final Random random = new Random();

    @Autowired private PassengerCountRepository passengerCountRepository;
    @Autowired private AlertRepository alertRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private RouteRepository routeRepository;
    @Autowired private StopRepository stopRepository;

    // Guardar resultado da última execução de cada pipeline
    private Map<String, Object> lastOccupancyResult  = new HashMap<>();
    private Map<String, Object> lastAlertsResult     = new HashMap<>();
    private Map<String, Object> lastRouteMetricsResult = new HashMap<>();

    // ── PIPELINES AGENDADOS ──────────────────────────────

    @Scheduled(fixedDelay = 30000, initialDelay = 5000)
    public void ingestPassengerOccupancy() {
        lastOccupancyResult = runOccupancyPipeline();
    }

    @Scheduled(fixedDelay = 60000, initialDelay = 10000)
    public void ingestSimulatedAlerts() {
        lastAlertsResult = runAlertsPipeline();
    }

    @Scheduled(fixedDelay = 120000, initialDelay = 15000)
    public void ingestRouteHealthMetrics() {
        lastRouteMetricsResult = runRouteMetricsPipeline();
    }

    // ── LÓGICA DOS PIPELINES (reutilizável) ──────────────

    public Map<String, Object> runOccupancyPipeline() {
        Map<String, Object> result = new HashMap<>();
        result.put("pipeline", "occupancy");
        result.put("startedAt", LocalDateTime.now());

        try {
            logger.info("[5.1.1] Executando pipeline de ocupação...");
            List<Vehicle> vehicles = vehicleRepository.findAll();
            int count = 0;

            for (Vehicle vehicle : vehicles) {
                if ("IN_SERVICE".equals(vehicle.getStatus()) && vehicle.getCapacity() != null && vehicle.getCapacity() > 0) {
                    int entry    = random.nextInt(5);
                    int exit     = random.nextInt(3);
                    int occupancy = Math.max(0, Math.min(vehicle.getCapacity(),
                            (int)(vehicle.getCapacity() * 0.6) + entry - exit));

                    PassengerCount pc = new PassengerCount();
                    pc.setVehicle(vehicle);
                    pc.setRoute(vehicle.getRoute());
                    pc.setStop(null);
                    pc.setEntryCount(entry);
                    pc.setExitCount(exit);
                    pc.setOccupancy(occupancy);
                    pc.setOccupancyPercentage((double) occupancy / vehicle.getCapacity() * 100);
                    pc.setTimestamp(LocalDateTime.now());
                    passengerCountRepository.save(pc);
                    count++;
                }
            }

            result.put("status", "SUCCESS");
            result.put("recordsIngested", count);
            result.put("finishedAt", LocalDateTime.now());
            logger.info("[5.1.1] Pipeline ocupação: {} registos ingeridos", count);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            logger.error("[5.1.1] Erro: {}", e.getMessage(), e);
        }

        lastOccupancyResult = result;
        return result;
    }

    public Map<String, Object> runAlertsPipeline() {
        Map<String, Object> result = new HashMap<>();
        result.put("pipeline", "alerts");
        result.put("startedAt", LocalDateTime.now());

        try {
            logger.info("[5.1.2] Executando pipeline de alertas...");
            List<Vehicle> vehicles = vehicleRepository.findAll();
            List<Route>   routes   = routeRepository.findAll();

            boolean alertCreated = false;

            if (!vehicles.isEmpty() && !routes.isEmpty() && random.nextDouble() < 0.5) {
                Vehicle vehicle = vehicles.get(random.nextInt(vehicles.size()));
                String[] types      = {"Tráfego Congestionado", "Avaria de Veículo", "Segurança", "Manutenção", "Passageiros"};
                String[] severities = {"LOW", "MEDIUM", "HIGH"};

                Alert alert = new Alert();
                alert.setType(types[random.nextInt(types.length)]);
                alert.setDescription("Alerta gerado pelo pipeline de ingestão");
                alert.setSource("VEHICLE:" + vehicle.getId());
                alert.setSeverity(severities[random.nextInt(severities.length)]);
                alert.setStatus("ACTIVE");
                alert.setCreatedBy("SISTEMA");
                alert.setCreatedAt(LocalDateTime.now());
                alertRepository.save(alert);
                alertCreated = true;
                logger.info("[5.1.2] Alerta criado: {}", alert.getType());
            }

            result.put("status", "SUCCESS");
            result.put("alertCreated", alertCreated);
            result.put("finishedAt", LocalDateTime.now());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            logger.error("[5.1.2] Erro: {}", e.getMessage(), e);
        }

        lastAlertsResult = result;
        return result;
    }

    public Map<String, Object> runRouteMetricsPipeline() {
        Map<String, Object> result = new HashMap<>();
        result.put("pipeline", "route-metrics");
        result.put("startedAt", LocalDateTime.now());

        try {
            logger.info("[5.1.3] Executando pipeline de métricas de rotas...");
            List<Route> routes = routeRepository.findAll();
            Map<String, Object> metrics = new HashMap<>();

            for (Route route : routes) {
                long vehicleCount = route.getVehicles() != null ? route.getVehicles().size() : 0;
                long stopCount    = route.getStops()    != null ? route.getStops().size()    : 0;
                metrics.put("route_" + route.getCode(), Map.of(
                    "status",    route.getStatus(),
                    "vehicles",  vehicleCount,
                    "stops",     stopCount,
                    "timestamp", LocalDateTime.now().toString()
                ));
            }

            result.put("status", "SUCCESS");
            result.put("routesProcessed", routes.size());
            result.put("metrics", metrics);
            result.put("finishedAt", LocalDateTime.now());
            logger.info("[5.1.3] Métricas de {} rotas capturadas", routes.size());
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            logger.error("[5.1.3] Erro: {}", e.getMessage(), e);
        }

        lastRouteMetricsResult = result;
        return result;
    }

    // ── ESTATÍSTICAS E ÚLTIMOS RESULTADOS ────────────────

    public Map<String, Object> getIngestionStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("passengerCountRecords", passengerCountRepository.count());
        stats.put("alertRecords",          alertRepository.count());
        stats.put("vehiclesMonitored",     vehicleRepository.count());
        stats.put("routesMonitored",       routeRepository.count());
        stats.put("lastIngestionTime",     LocalDateTime.now());
        stats.put("pipelineStatus",        "RUNNING");
        stats.put("lastOccupancyRun",      lastOccupancyResult);
        stats.put("lastAlertsRun",         lastAlertsResult);
        stats.put("lastRouteMetricsRun",   lastRouteMetricsResult);
        return stats;
    }
}
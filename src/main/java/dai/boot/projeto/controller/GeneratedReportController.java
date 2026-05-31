package dai.boot.projeto.controller;

import dai.boot.projeto.entities.GeneratedReport;
import dai.boot.projeto.entities.Route;
import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.repository.GeneratedReportRepository;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/generated-reports")
@CrossOrigin(origins = "*")
public class GeneratedReportController {

    private final GeneratedReportRepository generatedReportRepository;
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;

    public GeneratedReportController(
            GeneratedReportRepository generatedReportRepository,
            VehicleRepository vehicleRepository,
            RouteRepository routeRepository
    ) {
        this.generatedReportRepository = generatedReportRepository;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
    }

    @GetMapping
    public List<GeneratedReport> getAllGeneratedReports() {
        return generatedReportRepository.findAll();
    }

    @GetMapping("/generate")
    public ResponseEntity<GeneratedReport> generateAutomaticReport(
            @RequestParam(defaultValue = "system-overview") String type
    ) {
        List<Vehicle> vehicles = vehicleRepository.findAll();
        List<Route> routes = routeRepository.findAll();

        long totalVehicles = vehicles.size();
        long inService = vehicles.stream().filter(v -> "IN_SERVICE".equals(v.getStatus())).count();
        long outOfService = vehicles.stream().filter(v -> "OUT_OF_SERVICE".equals(v.getStatus())).count();
        long maintenanceVehicles = vehicles.stream().filter(v -> "MAINTENANCE".equals(v.getStatus())).count();

        long totalRoutes = routes.size();
        long activeRoutes = routes.stream().filter(r -> "ACTIVE".equals(r.getStatus())).count();
        long inactiveRoutes = routes.stream().filter(r -> "INACTIVE".equals(r.getStatus())).count();
        long maintenanceRoutes = routes.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();

        String content = """
                Relatório Automático
                Tipo: %s
                Gerado em: %s

                Veículos:
                - Total: %d
                - Em serviço: %d
                - Fora de serviço: %d
                - Em manutenção: %d

                Rotas:
                - Total: %d
                - Ativas: %d
                - Inativas: %d
                - Em manutenção: %d
                """.formatted(
                type,
                LocalDateTime.now(),
                totalVehicles,
                inService,
                outOfService,
                maintenanceVehicles,
                totalRoutes,
                activeRoutes,
                inactiveRoutes,
                maintenanceRoutes
        );

        GeneratedReport report = new GeneratedReport();
        report.setReportType(type);
        report.setContent(content);
        report.setGeneratedAt(LocalDateTime.now());
        report.setGeneratedBySystem(true);

        GeneratedReport saved = generatedReportRepository.save(report);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneratedReport> getReportById(@PathVariable Long id) {
        return generatedReportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteReport(@PathVariable Long id) {
        if (!generatedReportRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        generatedReportRepository.deleteById(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Relatório apagado com sucesso.");
        return ResponseEntity.ok(response);
    }
}
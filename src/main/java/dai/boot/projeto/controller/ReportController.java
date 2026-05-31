package dai.boot.projeto.controller;

import dai.boot.projeto.entities.PassengerCount;
import dai.boot.projeto.entities.Route;
import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.repository.VehicleRepository;
import dai.boot.projeto.service.PassengerService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;
    private final PassengerService passengerService;

    public ReportController(
            VehicleRepository vehicleRepository,
            RouteRepository routeRepository,
            PassengerService passengerService
    ) {
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.passengerService = passengerService;
    }

    @GetMapping("/vehicles")
    public ResponseEntity<Map<String, Object>> getVehiclesReport() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        long total = vehicles.size();
        long inService = vehicles.stream().filter(v -> "IN_SERVICE".equals(v.getStatus())).count();
        long outOfService = vehicles.stream().filter(v -> "OUT_OF_SERVICE".equals(v.getStatus())).count();
        long maintenance = vehicles.stream().filter(v -> "MAINTENANCE".equals(v.getStatus())).count();

        Map<String, Object> report = new HashMap<>();
        report.put("type", "vehicles");
        report.put("total", total);
        report.put("inService", inService);
        report.put("outOfService", outOfService);
        report.put("maintenance", maintenance);
        report.put("items", vehicles);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/routes")
    public ResponseEntity<Map<String, Object>> getRoutesReport() {
        List<Route> routes = routeRepository.findAll();

        long total = routes.size();
        long active = routes.stream().filter(r -> "ACTIVE".equals(r.getStatus())).count();
        long inactive = routes.stream().filter(r -> "INACTIVE".equals(r.getStatus())).count();
        long maintenance = routes.stream().filter(r -> "MAINTENANCE".equals(r.getStatus())).count();

        Map<String, Object> report = new HashMap<>();
        report.put("type", "routes");
        report.put("total", total);
        report.put("active", active);
        report.put("inactive", inactive);
        report.put("maintenance", maintenance);
        report.put("items", routes);

        return ResponseEntity.ok(report);
    }

    @GetMapping("/passengers")
    public ResponseEntity<Map<String, Object>> getPassengersReport(
            @RequestParam Long panelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<PassengerCount> counts = passengerService.getCounts(panelId, from, to);
        int occupancy = passengerService.calculateOccupancy(panelId, from, to);
        Map<String, Object> summary = passengerService.getSummary(panelId);

        Map<String, Object> report = new HashMap<>();
        report.put("type", "passengers");
        report.put("panelId", panelId);
        report.put("from", from);
        report.put("to", to);
        report.put("occupancy", occupancy);
        report.put("summary", summary);
        report.put("items", counts);

        return ResponseEntity.ok(report);
    }
}
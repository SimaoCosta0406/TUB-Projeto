package dai.boot.projeto.controller;

import dai.boot.projeto.entities.Route;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private final RouteRepository routeRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public RouteController(RouteRepository routeRepository, VehicleRepository vehicleRepository) {
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping
    public ResponseEntity<List<Route>> getAll() {
        return ResponseEntity.ok(routeRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getById(@PathVariable Long id) {
        Optional<Route> opt = routeRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<Route> getByCode(@PathVariable String code) {
        Optional<Route> opt = routeRepository.findByCode(code);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Route> create(@RequestBody Route route) {
        // Validação básica: o código da rota é obrigatório e único
        if (route.getCode() == null || route.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Verificar existência de código igual
        Optional<Route> existente = routeRepository.findByCode(route.getCode().trim());
        if (existente.isPresent()) {
            return ResponseEntity.status(409).build(); // Conflito: código já existente
        }

        Route saved = routeRepository.save(route);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> update(@PathVariable Long id, @RequestBody Route routeDetails) {
        Optional<Route> opt = routeRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Route route = opt.get();
        // Se tentou alterar o código, verificar conflito
        String newCode = routeDetails.getCode();
        if (newCode != null && !newCode.equals(route.getCode())) {
            Optional<Route> other = routeRepository.findByCode(newCode);
            if (other.isPresent() && !other.get().getId().equals(id)) {
                return ResponseEntity.status(409).build();
            }
            route.setCode(newCode);
        }
        route.setOrigin(routeDetails.getOrigin());
        route.setDestination(routeDetails.getDestination());
        route.setStops(routeDetails.getStops());
        route.setStatus(routeDetails.getStatus());
        route.setMetadata(routeDetails.getMetadata());

        return ResponseEntity.ok(routeRepository.save(route));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!routeRepository.existsById(id)) return ResponseEntity.notFound().build();
        routeRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<Vehicle>> listVehiclesForRoute(@PathVariable Long id) {
        Optional<Route> opt = routeRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Route route = opt.get();
        List<Vehicle> vehicles = vehicleRepository.findByRoute(route);
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Route>> listByStatus(@PathVariable String status) {
        return ResponseEntity.ok(routeRepository.findByStatus(status));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Route> changeStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Route> opt = routeRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Route r = opt.get();
        r.setStatus(status);
        return ResponseEntity.ok(routeRepository.save(r));
    }

    // -------- GERENCIAMENTO DE VEÍCULOS (BIDIRECIONAL) --------

    /**
     * Adiciona um veículo a esta rota.
     * O veículo será automaticamente associado à rota.
     */
    @PostMapping("/{routeId}/vehicles/{vehicleId}")
    public ResponseEntity<Route> addVehicleToRoute(@PathVariable Long routeId, @PathVariable Long vehicleId) {
        Optional<Route> routeOpt = routeRepository.findById(routeId);
        if (routeOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) return ResponseEntity.notFound().build();

        Route route = routeOpt.get();
        Vehicle vehicle = vehicleOpt.get();
        
        // Adiciona o veículo à rota (mantém bidirecionalidade automaticamente)
        route.addVehicle(vehicle);
        
        return ResponseEntity.ok(routeRepository.save(route));
    }

    /**
     * Remove um veículo desta rota.
     * O veículo será automaticamente desassociado da rota.
     */
    @DeleteMapping("/{routeId}/vehicles/{vehicleId}")
    public ResponseEntity<Route> removeVehicleFromRoute(@PathVariable Long routeId, @PathVariable Long vehicleId) {
        Optional<Route> routeOpt = routeRepository.findById(routeId);
        if (routeOpt.isEmpty()) return ResponseEntity.notFound().build();
        
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) return ResponseEntity.notFound().build();

        Route route = routeOpt.get();
        Vehicle vehicle = vehicleOpt.get();
        
        // Remove o veículo da rota (mantém bidirecionalidade automaticamente)
        route.removeVehicle(vehicle);
        
        return ResponseEntity.ok(routeRepository.save(route));
    }
}
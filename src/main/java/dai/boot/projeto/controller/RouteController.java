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
        try {
            Route saved = routeRepository.save(route);
            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(saved.getId())
                    .toUri())
                    .body(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> update(@PathVariable Long id, @RequestBody Route routeDetails) {
        Optional<Route> opt = routeRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Route route = opt.get();
        route.setCode(routeDetails.getCode());
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
        List<Vehicle> vehicles = vehicleRepository.findByRoute_Code(opt.get().getCode());
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
}
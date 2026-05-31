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
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")
public class VehicleController {

    // CORRIGIDO: VehicleRepesitory -> VehicleRepository
    private final VehicleRepository vehicleRepository;
    private final RouteRepository routeRepository;

    @Autowired
    public VehicleController(VehicleRepository vehicleRepository, RouteRepository routeRepository) {
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
    }

    @GetMapping
    public ResponseEntity<List<Vehicle>> getAll() {
        return ResponseEntity.ok(vehicleRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id) {
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CORRIGIDO: getByPlane -> getByPlate
    @GetMapping("/plate/{plate}")
    public ResponseEntity<Vehicle> getByPlate(@PathVariable String plate) {
        Optional<Vehicle> opt = vehicleRepository.findByPlate(plate);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Vehicle> create(@RequestBody Vehicle vehicle) {
        try {
            Vehicle saved = vehicleRepository.save(vehicle);
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
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @RequestBody Vehicle vehicleDetails) {
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Vehicle vehicle = opt.get();
        vehicle.setPlate(vehicleDetails.getPlate());
        vehicle.setModel(vehicleDetails.getModel());
        vehicle.setStatus(vehicleDetails.getStatus());
        vehicle.setCapacity(vehicleDetails.getCapacity());
        vehicle.setMetadata(vehicleDetails.getMetadata());
        // NÃO sobrescrever a rota — é gerida pelo assign-route/unassign-route

        return ResponseEntity.ok(vehicleRepository.save(vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!vehicleRepository.existsById(id)) return ResponseEntity.notFound().build();
        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<Vehicle> changeStatus(@PathVariable Long id, @RequestParam String status) {
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Vehicle v = opt.get();
        v.setStatus(status);
        return ResponseEntity.ok(vehicleRepository.save(v));
    }

    @PostMapping("/{id}/assign-route/{routeId}")
    public ResponseEntity<Vehicle> assignRoute(@PathVariable Long id, @PathVariable Long routeId) {
        Optional<Vehicle> vOpt = vehicleRepository.findById(id);
        if (vOpt.isEmpty()) return ResponseEntity.notFound().build();
        Optional<Route> rOpt = routeRepository.findById(routeId);
        if (rOpt.isEmpty()) return ResponseEntity.notFound().build();

        Vehicle v = vOpt.get();
        v.setRoute(rOpt.get());
        // CORRIGIDO: vehicleRepository.sabe(v) -> vehicleRepository.save(v)
        return ResponseEntity.ok(vehicleRepository.save(v));
    }

    @PostMapping("/{id}/assign-route-by-code")
    // CORRIGIDO: Spring code -> String code
    public ResponseEntity<Vehicle> assignRouteByCode(@PathVariable Long id, @RequestParam("code") String code) {
        Optional<Vehicle> vOpt = vehicleRepository.findById(id);
        if (vOpt.isEmpty()) return ResponseEntity.notFound().build();
        Optional<Route> rOpt = routeRepository.findByCode(code);
        if (rOpt.isEmpty()) return ResponseEntity.notFound().build();

        Vehicle v = vOpt.get();
        v.setRoute(rOpt.get());
        return ResponseEntity.ok(vehicleRepository.save(v));
    }

    @PostMapping("/{id}/unassign-route")
    public ResponseEntity<Vehicle> unassignRoute(@PathVariable Long id) {
        Optional<Vehicle> vOpt = vehicleRepository.findById(id);
        if (vOpt.isEmpty()) return ResponseEntity.notFound().build();
        Vehicle v = vOpt.get();
        v.setRoute(null);
        return ResponseEntity.ok(vehicleRepository.save(v));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Vehicle>> findByStatus(@PathVariable String status) {
        return ResponseEntity.ok(vehicleRepository.findByStatus(status));
    }

    @GetMapping("/by-capacity")
    public ResponseEntity<List<Vehicle>> findByCapacity(@RequestParam("min") Integer min) {
        return ResponseEntity.ok(vehicleRepository.findByCapacityGreaterThanEqual(min));
    }

    @GetMapping("/search/model")
    public ResponseEntity<List<Vehicle>> searchByModel(@RequestParam("q") String q) {
        return ResponseEntity.ok(vehicleRepository.findByModelContainingIgnoreCase(q));
    }

    @GetMapping("/by-route")
    public ResponseEntity<List<Vehicle>> findByRouteCode(@RequestParam("code") String code) {
        return ResponseEntity.ok(vehicleRepository.findByRoute_Code(code));
    }
}

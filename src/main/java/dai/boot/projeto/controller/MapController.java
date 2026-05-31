package dai.boot.projeto.controller;

import dai.boot.projeto.entities.Stop;
import dai.boot.projeto.repository.StopRepository;
import dai.boot.projeto.repository.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/stops")
public class MapController {

    private final StopRepository stopRepository;
    private final VehicleRepository vehicleRepository;

    public MapController(StopRepository stopRepository, VehicleRepository vehicleRepository) {
        this.stopRepository = stopRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping
    public List<Stop> getStops() {
        return stopRepository.findAll();
    }

    @PostMapping
    public Stop criarStop(@RequestBody Stop stop) {
        return stopRepository.save(stop);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Stop> editarStop(@PathVariable Long id, @RequestBody Stop dados) {
        Optional<Stop> stopOpt = stopRepository.findById(id);

        if (stopOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Stop stop = stopOpt.get();
        stop.setName(dados.getName());
        stop.setLatitude(dados.getLatitude());
        stop.setLongitude(dados.getLongitude());

        return ResponseEntity.ok(stopRepository.save(stop));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagarStop(@PathVariable Long id) {
        if (!stopRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        stopRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<?>> getVehiclesByStop(@PathVariable Long id) {
        Optional<Stop> stopOpt = stopRepository.findById(id);
        if (stopOpt.isEmpty()) return ResponseEntity.notFound().build();

        Stop stop = stopOpt.get();
        List<?> vehicles = vehicleRepository.findByRouteStopsContaining(stop.getName());
        return ResponseEntity.ok(vehicles);
    }
}
package main.java.dai.boot.projeto.controller;


import dai.boot.projeto.entities.Route;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;


import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*")


public class VehicleController {
    private final VehicleRepesitory vehicleRepository;
    private final RouteRepository routeRepository;


    @Autowired
    public VehicleController(VehicleRepository vehicleRepository, RouteRepository routeRepository){
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
    }


    @GetMapping
    public ResponseEntity<List<Vehicle>> getAll(){
        List<Vehicle> vehicles = vehicleRepository.findAll();
        return ResponseEntity.ok(vehicles);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Vehicle> getById(@PathVariable Long id){
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() ->ResponseEntity.notFound().build());
    }


    @GetMapping("/plate/{plate}")
    public ResponseEntity<Vehicle> getByPlane(@PathVariable String plate){
        Optional<Vehicle> opt = vehicleRepository.findByPlate(plate);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping
    public ResponseEntity<Vehicle> create(@Valid @RequestBody Vehicle vehicle){
        if (vehicle.getPlate() != null && vehicleRepository.findByPlate(vehicle.getPlate()).isPresent()){
            return ResponseEntity.status(409).build();
        }
        Vehicle saved = vehicleRepository.save(vehicle);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Vehicle> update(@PathVariable Long id, @Valid @RequestBody Vehicle updated){
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if(opt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Vehicle existing = opt.get();


        if(updated.getPlate() != null && !updated.getPlate().equals(existing.getPlate())){
            if(vehicleRepository.findByPlate(updated.getPlate()).isPresent()){
                return ResponseEntity.status(409).build();
            }
            existing.setPlate(updated.getPlate());
        }
       
        if (updated.getModel() != null){
            existing.setModel(updated.getModel());
        }


        if (updated.getCapacity() != null){
            existing.setCapacity(updated.getCapacity());
        }


        if(updated.getStatus() != null){
            existing.setStatus(updated.getStatus());
        }


        if(updated.getMetadata() != null){
            existing.setMetadata(updated.getMetadata());
        }


        Vehicle saved = vehicleRepository.save(existing);
        return ResponseEntity.ok(saved);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        if(!vehicleRepository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        vehicleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/status")
    public ResponseEntity<Vehicle> changeStatus(@PathVariable Long id, @RequestParam String status){
        Optional<Vehicle> opt = vehicleRepository.findById(id);
        if(opt.isEmpty()){
            return ResponseEntity.notFound().build();
        }


        Vehicle v = opt.get();
        v.setStatus(status);
        Vehicle saved = vehicleRepository.save(v);
        return ResponseEntity.ok(saved);
    }


    @PostMapping("/{id}/assign-route/{routeId}")
    public ResponseEntity<Vehicle> assignRoute(@PathVariable Long id, @PathVariable Long routeId){
        Optional<Vehicle> vOpt= vehicleRepository.findById(id);
        if(vOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Optional<Route> rOpt= routeRepository.findById(routeId);
        if(rOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Vehicle v = vOpt.get();
        v.setRoute(rOpt.get());
        vehicleRepository.sabe(v);
        return ResponseEntity.ok(v);  
    }


    @PostMapping("/{id}/assign-route-by-code")
    public ResponseEntity<Vehicle> assignRouteByCode(@PathVariable Long id, @RequestParam("code") Spring code){
        Optional<Vehicle> vOpt = vehicleRepository.findById(id);
        if(vOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Optional<Route> rOpt = routeRepository.findByCode(code);
        if(rOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Vehicle v = vOpt.get();
        v.setRoute(rOpt.get());
        vehicleRepository.save(v);
        return ResponseEntity.ok(v);
    }


    @PostMapping("/{id}/unassign-route")
    public ResponseEntity<Vehicle> unassignRoute(@PathVariable Long id){
        Optional<Vehicle> vOpt = vehicleRepository.findById(id);
        if(vOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Vehicle v = vOpt.get();
        v.setRoute(null);
        vehicleRepository.save(v);
        return ResponseEntity.ok(v);
    }


    @GetMapping("/status/{status}")
    public ResponseEntity<List<Vehicle>> findByStatus(@PathVariable String status){
        List<Vehicle> vehicles = vehicleRepository.findByStatus(status);
        return ResponseEntity.ok(vehicles);
    }


    @GetMapping("/by-capacity")
    public ResponseEntity<List<Vehicle>> findByCapacity(@RequestParam("min") Integer min){
        List<Vehicle> list = vehicleRepository.findByCapacityGreaterThanEqual(min);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/search/model")
    public ResponseEntity<List<Vehicle>> searchByModel(@RequestParam("q") String q){
        List<Vehicle> list = vehicleRepository.findByModelContainingIgnoreCase(q);
        return ResponseEntity.ok(list);
    }


    @GetMapping("/by-route")
    public ResponseEntity<List<Vehicle>> findByRouteCode(@RequestParam("code") String code){
        List<Vehicle> list = vehicleRepository.findByRoute_Code(code);
        return ResponseEntity.ok(list);
    }
}

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

import java.net.ResponseCache;
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
    public RouteController(RouteRepository routeRepository, VehicleRepository vehicleRepository){
        this.routeRepository = routeRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Route> getById(@PathVariable Long id){
        Optional<Route> opt = routeRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<Route> getByCode(@PathVariable String code){
        Optional<Route> opt = routeRepository.findByCode(code);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Route> create(@Valid @RequestBody Route route){
        if(route.getCode() != null && routeRepository.findByCode(route.getCode()).isPresent()){
            return ResponseEntity.badRequest().build();
        }
        Route savedRoute = routeRepository.save(route);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedRoute.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Route> update(@PathVariable Long id, @Valid @RequestBody Route updated){
        Optional<Route> opt = routeRepository.findById(id);
        if(opt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Route existing = opt.get();

        if(updated.getCode() != null && !updated.getCode().equals(existing.getCode())){
            if(routeRepository.findByCode(updated.getCode()).isPresent()){
                return ResponseEntity.badRequest().build();
            }
            existing.setCode(updated.getCode());
        }

        if(updated.getOrigin() != null) existing.setOrigin(updated.getOrigin());
        if(updated.getDestination() != null) existing.setDestination(updated.getDestination());
        if(updated.getStops() != null) existing.setStops(updated.getStops());
        if(updated.getStatus() != null) existing.setStatus(updated.getStatus());
        if(updated.getMetadata() != null) existing.setMetadata(updated.getMetadata());

        Route saved = routeRepository.save(existing);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/vehicles")
    public ResponseEntity<List<Vehicle>> listVehiclesForRoute(@PathVariable Long id){
        Optional<Route> opt = routeRepository.findById(id);
        if(opt.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        Route route = opt.get();
        List<Vehicle> vehicles = vehicleRepository.findByRoute_Code(route.getCode());
        return ResponseEntity.ok(vehicles);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Route>> listByStatus(@PathVariable String status){
        List<Route> routes = routeRepository.findByStatus(status);
        return ResponseEntity.ok(routes);
    }
}

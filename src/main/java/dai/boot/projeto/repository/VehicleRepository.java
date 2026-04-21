package main.java.dai.boot.projeto.repository;

import dai.boot.projeto.entities.Vehicle;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlate(String plate);
    List<Vehicle> findByStatus(String status);
    List<Vehicle> findByCapacityGreaterThanEqual(Integer capacity);
    List<Vehicle> findByModelContainingIgnoreCase(String modelPart);
    List<Vehicle> findByRoute_Code(String routeCode);
}

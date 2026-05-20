package dai.boot.projeto.repository;

import dai.boot.projeto.entities.Vehicle;
import dai.boot.projeto.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// CORRIGIDO: faltava import de List
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlate(String plate);
    List<Vehicle> findByStatus(String status);
    List<Vehicle> findByCapacityGreaterThanEqual(Integer capacity);
    List<Vehicle> findByModelContainingIgnoreCase(String modelPart);
    List<Vehicle> findByRoute_Code(String routeCode);
    List<Vehicle> findByRoute(Route route);

    @Query("SELECT v FROM Vehicle v JOIN v.route r JOIN r.stops s WHERE s = :stopName")
    List<Vehicle> findByRouteStopsContaining(@Param("stopName") String stopName);
}

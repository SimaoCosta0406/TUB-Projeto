package dai.boot.projeto.repository;

import dai.boot.projeto.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
// CORRIGIDO: org.springframework.sterotype -> org.springframework.stereotype
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByCode(String code);
    List<Route> findByStatus(String status);
    List<Route> findByOriginContainingIgnoreCase(String originPart);
    List<Route> findByDestinationContainingIgnoreCase(String destinationPart);
}
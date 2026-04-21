package dai.boot.projeto.repository;

import dai.boot.projeto.entities.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(String status);
    List<Alert> findByTypeAndStatus(String type, String status);
    long countBySeverityAndStatus(String severity, String status);
}

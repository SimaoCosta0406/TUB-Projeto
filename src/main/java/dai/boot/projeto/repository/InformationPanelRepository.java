package dai.boot.projeto.repository;

import dai.boot.projeto.entities.InformationPanel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface InformationPanelRepository extends JpaRepository<InformationPanel, Long> {
    List<InformationPanel> findByRouteId(Long routeId);
    List<InformationPanel> findByStatus(String status);
    List<InformationPanel> findByStop(dai.boot.projeto.entities.Stop stop);
}
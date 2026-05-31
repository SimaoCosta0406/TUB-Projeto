package dai.boot.projeto.repository;

import dai.boot.projeto.entities.PassengerCount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;


public interface PassengerCountRepository extends JpaRepository<PassengerCount, Long> {
    List<PassengerCount> findByPanelIdAndTimestampBetween(Long panelId, LocalDateTime from, LocalDateTime to);
    List<PassengerCount> findByTimestampAfter(LocalDateTime timestamp);
    long countByTimestampAfter(LocalDateTime timestamp);
}
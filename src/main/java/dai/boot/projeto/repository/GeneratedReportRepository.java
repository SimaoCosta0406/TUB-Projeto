package dai.boot.projeto.repository;

import dai.boot.projeto.entities.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, Long> {
}
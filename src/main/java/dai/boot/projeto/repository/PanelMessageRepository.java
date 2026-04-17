package dai.boot.projeto.repository;

import dai.boot.projeto.entities.PanelMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanelMessageRepository extends JpaRepository<PanelMessage, Long> {
}
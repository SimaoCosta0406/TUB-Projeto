package dai.boot.projeto.controller;

import dai.boot.projeto.entities.InformationPanel;
import dai.boot.projeto.repository.InformationPanelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/panels")
public class PanelController {

    @Autowired
    private InformationPanelRepository panelRepository;

    @GetMapping
    public List<InformationPanel> getAllPanels() {
        return panelRepository.findAll();
    }
}
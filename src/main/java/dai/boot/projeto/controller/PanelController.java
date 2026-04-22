package dai.boot.projeto.controller;

import dai.boot.projeto.entities.InformationPanel;
import dai.boot.projeto.entities.Route;
import dai.boot.projeto.repository.InformationPanelRepository;
import dai.boot.projeto.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/panels")
public class PanelController {

    @Autowired
    private InformationPanelRepository panelRepository;

    @Autowired
    private RouteRepository routeRepository;

    @GetMapping
    public List<InformationPanel> getAllPanels() {
        return panelRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<InformationPanel> createPanel(@RequestBody InformationPanel panel) {
        try {
            // Se houver um routeId no request, validar se existe
            if (panel.getRoute() != null && panel.getRoute().getId() != null) {
                Optional<Route> route = routeRepository.findById(panel.getRoute().getId());
                if (route.isPresent()) {
                    panel.setRoute(route.get());
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
            InformationPanel savedPanel = panelRepository.save(panel);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPanel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<InformationPanel> updatePanel(@PathVariable Long id, @RequestBody InformationPanel panelDetails) {
        try {
            Optional<InformationPanel> existingPanel = panelRepository.findById(id);
            if (existingPanel.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            InformationPanel panel = existingPanel.get();
            
            // Atualizar campos básicos
            if (panelDetails.getLocation() != null) panel.setLocation(panelDetails.getLocation());
            if (panelDetails.getAddress() != null) panel.setAddress(panelDetails.getAddress());
            if (panelDetails.getManufacturer() != null) panel.setManufacturer(panelDetails.getManufacturer());
            if (panelDetails.getPanelType() != null) panel.setPanelType(panelDetails.getPanelType());
            if (panelDetails.getStatus() != null) panel.setStatus(panelDetails.getStatus());
            if (panelDetails.getConnectivityType() != null) panel.setConnectivityType(panelDetails.getConnectivityType());
            if (panelDetails.getStop() != null) panel.setStop(panelDetails.getStop());
            
            // Atualizar rota
            if (panelDetails.getRoute() != null) {
                if (panelDetails.getRoute().getId() != null) {
                    Optional<Route> route = routeRepository.findById(panelDetails.getRoute().getId());
                    if (route.isPresent()) {
                        panel.setRoute(route.get());
                    } else {
                        return ResponseEntity.badRequest().build();
                    }
                } else {
                    panel.setRoute(null);
                }
            }

            InformationPanel updatedPanel = panelRepository.save(panel);
            return ResponseEntity.ok(updatedPanel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<InformationPanel> getPanelById(@PathVariable Long id) {
        Optional<InformationPanel> panel = panelRepository.findById(id);
        if (panel.isPresent()) {
            return ResponseEntity.ok(panel.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
package dai.boot.projeto.controller;

import dai.boot.projeto.entities.InformationPanel;
import dai.boot.projeto.entities.Route;
import dai.boot.projeto.entities.Stop;
import dai.boot.projeto.repository.InformationPanelRepository;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.repository.StopRepository;
import dai.boot.projeto.service.PanelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/panels")
@CrossOrigin(origins = "*")
public class PanelController {

    @Autowired
    private InformationPanelRepository panelRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private dai.boot.projeto.repository.PanelMessageRepository panelMessageRepository;

    @Autowired
    private PanelService panelService;

    @GetMapping
    public List<InformationPanel> getAllPanels() {
        return panelRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<InformationPanel> createPanel(@RequestBody InformationPanel panel) {
        try {
            if (panel.getRoute() != null && panel.getRoute().getId() != null) {
                Optional<Route> route = routeRepository.findById(panel.getRoute().getId());
                if (route.isPresent()) {
                    panel.setRoute(route.get());
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
            if (panel.getStop() != null && panel.getStop().getId() != null) {
                Optional<Stop> stop = stopRepository.findById(panel.getStop().getId());
                if (stop.isPresent()) {
                    panel.setStop(stop.get());
                } else {
                    return ResponseEntity.badRequest().build();
                }
            }
            if (panel.getStatus() == null || panel.getStatus().isBlank()) {
                panel.setStatus("ACTIVE");
            }
            if (panel.getInstallationDate() == null) {
                panel.setInstallationDate(LocalDateTime.now());
            }
            panel.setLastUpdated(LocalDateTime.now());
            InformationPanel savedPanel = panelRepository.save(panel);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedPanel);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createPanelSimple(@RequestBody Map<String, Object> request){
        try{
            String name= (String) request.get("name");
            Long stopId = Long.valueOf(request.get("stopId").toString());

            InformationPanel panel = panelService.createPanel(name, stopId);
            return ResponseEntity.status(HttpStatus.CREATED).body(panel);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
            if (panelDetails.getStop() != null) {
                if (panelDetails.getStop().getId() != null) {
                    Optional<Stop> stop = stopRepository.findById(panelDetails.getStop().getId());
                    if (stop.isPresent()) {
                        panel.setStop(stop.get());
                    } else {
                        return ResponseEntity.badRequest().build();
                    }
                } else {
                    panel.setStop(null);
                }
            }
            
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

            panel.setLastUpdated(LocalDateTime.now());
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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePanel(@PathVariable Long id){
        try{
            panelService.deletePanel(id);
            return ResponseEntity.ok(Map.of("message", "Painel Eliminado com Sucesso!"));
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error",e.getMessage()));
        }
    }

    @GetMapping("/{id}/realtime")
    public ResponseEntity<?> getPanelRealTimeInfo(@PathVariable Long id){
        try{
            Map<String, Object> info = panelService.getPanelRealTimeInfo(id);
            return ResponseEntity.ok(info);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stop/{stopId}/realtime")
    public ResponseEntity<?> getStopPanelsRealTimeInfo(@PathVariable Long stopId){
        try{
            Map<String, Object> info = panelService.getStopPanelsRealTimeInfo(stopId);
            return ResponseEntity.ok(info);
        }catch(Exception e){
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/stops")
    public ResponseEntity<List<Stop>> getAllStops() {
        return ResponseEntity.ok(panelService.getAllStops());
    }

    @PostMapping("/{id}/simulate-failure")
    public ResponseEntity<InformationPanel> simulateFailure(@PathVariable Long id) {
        return panelRepository.findById(id).map(panel -> {
            panel.setStatus("ERROR");
            panel.setLastUpdated(LocalDateTime.now());
            return ResponseEntity.ok(panelRepository.save(panel));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<dai.boot.projeto.entities.PanelMessage>> getPanelMessages(@PathVariable Long id) {
        return ResponseEntity.ok(panelMessageRepository.findAll().stream()
                .filter(m -> m.getPanel() != null && m.getPanel().getId().equals(id))
                .toList());
    }
}

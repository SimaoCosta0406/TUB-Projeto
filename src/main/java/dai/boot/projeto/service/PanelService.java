package main.java.dai.boot.projeto.service;

import dai.boot.projeto.entities.*;
import dai.boot.projeto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PanelService {
    
    @Autowired
    private InformationPanelRepository panelRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PassengerCountRepository passangerCountRepository;

    public List<InformationPanel> getAllPanels(){
        return panelRepository.findAll();
    }

    public Optional<InformationPanel> getPanelById(Long id){
        return panelRepository.findById(id);
    }

    public InformationPanel createPanel(String name, Long stopId){
        Stop stop = stopRepository.findById(stopId).orElseThrow(() -> new RuntimeException("Paragem não encontrada com id: " + stopId));

        InformationPanel panel = new InformationPanel();
        panel.setLocation(name);
        panel.setStop(stop);
        panel.setStatus("ACTIVE");
        panel.setInstallationDate(LocalDateTime.now());
        panel.setLastUpdated(LocalDateTime.now());

        return panelRepository.save(panel);
    }

    public void deletePanel(Long id){
        panelRepository.deleteById(id);
    }

    public Map<String, Object> getPanelRealTimeInfo(Long panelId) {
        InformationPanel panel = panelRepository.findById(panelId).orElseThrow(() -> new RuntimeException("Painel não encontrado com id: " + panelId));
        Stop stop = panel.getStop();
        if(stop == null) {
            throw new RuntimeException("Painel não tem paragem associada");
        }
        return getStopPanelsRealTimeInfo(stop.getId());
    }

    public Map<String, Object> getStopPanelsRealTimeInfo(Long stopId) {
        Stop stop = stopRepository.findById(stopId).orElseThrow(() -> new RuntimeException("Paragem não encontrada"));
        Map<String, Object> response = new HashMap<>();
        response.put("stopId", stop.getId());
        response.put("stopName", stop.getName());
        response.put("lastUpdate", LocalDateTime.now().toString());

        List<Route> allRoutes = routeRepository.findAll();
        List<Map<String, Object>> arrivals = new ArrayList<>();

        for(Route route : allRoutes) {
            if(route.getStops() == null || route.getStops().isEmpty()) {
                continue;
            }

            boolean passesStop = route.getStops().stream().anyMatch(s -> s.equalsIgnoreCase(stop.getName()));

            if(passesStop) {
                List<Vehicle> vehicles = vehicleRepository.findByRoute(route);
                for(Vehicle vehicle : vehicles) {
                    if(!vehicle.isInService()) {
                        continue;
                    }
                    Map<String, Object> arrivalInfo = new HashMap<>();
                    arrivalInfo.put("vehicleId", vehicle.getId());
                    arrivalInfo.put("vehiclePlate", vehicle.getPlate());
                    arrivalInfo.put("vehicleModel", vehicle.getModel());
                    arrivalInfo.put("routeCode", route.getCode());
                    arrivalInfo.put("routeDestination", route.getDestination());
                    arrivalInfo.put("capacity", vehicle.getCapacity() != null ? vehicle.getCapacity() : 0);

                    int currentOccupancy = calculateVehicleOccupancy(vehicle.getId());
                    arrivalInfo.put("currentOccupancy", currentOccupancy);

                    int etaMinutes = calculateETA(route, stop.getName());
                    arrivalInfo.put("etaMinutes", etaMinutes);

                    arrivals.add(arrivalInfo);
                }
            }
        }
        
        arrivals.sort(Comparator.comparingInt(a -> (int)a.get("etaMinutes")));
        response.put("arrivals", arrivals);
        return response;
    }

    private int calculateVehicleOccupancy(Long vehicleId) {
        return 0; // Placeholder - implementar com lógica real de contagem de passageiros
    }

    private int calculateETA(Route route, String stopName) {
        if(route.getStops() == null || route.getStops().isEmpty()) {
            return new Random().nextInt(15) + 1;
        }

        int stopIndex = -1;
        for(int i = 0; i < route.getStops().size(); i++) {
            if(route.getStops().get(i).equalsIgnoreCase(stopName)) {
                stopIndex = i;
                break;
            }
        }
        if(stopIndex < 0) {
            return new Random().nextInt(15) + 1;
        }

        int baseMinutes = (Math.abs(route.getCode().hashCode()) % 10) + 1;
        return Math.max(1, baseMinutes + (stopIndex % 5));
    }

    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }
}

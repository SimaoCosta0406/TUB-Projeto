package dai.boot.projeto.service;

import dai.boot.projeto.entities.*;
import dai.boot.projeto.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class PanelService {
    
    @Autowired
    private InformationPanelRepository panelRepository;

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private PassengerService passengerService;

    @PersistenceContext
    private EntityManager entityManager; // Usado para fazer a query nativa do GTFS TUB diretamente

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

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPanelRealTimeInfo(Long panelId) {
        InformationPanel panel = panelRepository.findById(panelId).orElseThrow(() -> new RuntimeException("Painel não encontrado com id: " + panelId));
        Stop stop = panel.getStop();
        if(stop == null) {
            throw new RuntimeException("Painel não tem paragem associada");
        }
        Map<String, Object> response = getStopPanelsRealTimeInfo(stop.getId());
        Map<String, Object> passengerCounts = passengerService.getCurrentCountForPanel(panelId);
        Route assignedRoute = panel.getRoute();
        if (assignedRoute == null) {
            assignedRoute = findRouteForStop(stop).orElse(null);
        }

        response.put("panelId", panelId);
        response.put("passengerCounts", passengerCounts);
        if (assignedRoute != null) {
            response.put("assignedRoute", routeToMap(assignedRoute));

            List<Map<String, Object>> arrivals = (List<Map<String, Object>>) response.get("arrivals");
            if (arrivals == null || arrivals.isEmpty()) {
                response.put("arrivals", List.of(routeToArrivalMap(assignedRoute)));
            }
        }

        return response;
    }

    /**
     * JUNTA OS DADOS EM TEMPO REAL INTERCALANDO AS TABELAS GTFS DA TUB
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getStopPanelsRealTimeInfo(Long stopId) {
        // 1. Procuramos a paragem na tua tabela local de painéis
        Stop stop = stopRepository.findById(stopId).orElseThrow(() -> new RuntimeException("Paragem não encontrada"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("stopId", stop.getId());
        response.put("stopName", stop.getName());
        response.put("lastUpdate", LocalDateTime.now().toString());

        // 2. Query nativa para cruzar as tabelas gtfs_ e descobrir os próximos autocarros para esta paragem
        // Usamos o cast ou id aproximado para cruzar com o stop_id do GTFS
        String sql = "SELECT r.route_short_name, r.route_long_name, t.trip_headsign, st.arrival_time " +
                     "FROM gtfs_stop_times st " +
                     "JOIN gtfs_trips t ON st.trip_id = t.trip_id " +
                     "JOIN gtfs_routes r ON t.route_id = r.route_id " +
                     "WHERE st.stop_id = :stopId " +
                     "ORDER BY st.arrival_time ASC LIMIT 5";

        List<Map<String, Object>> arrivals = new ArrayList<>();
        
        try {
            // Executa a query ligando o ID da tua paragem ao stop_id da TUB
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter("stopId", String.valueOf(stopId)) // Converte para String porque no GTFS é VARCHAR
                    .getResultList();

            LocalTime agora = LocalTime.now();

            for (Object[] row : rows) {
                Map<String, Object> arrivalInfo = new HashMap<>();
                String routeShortName = (String) row[0];
                String routeLongName = (String) row[1];
                String tripHeadsign = (String) row[2];
                String arrivalTimeStr = (String) row[3]; // No formato "HH:mm:ss"

                arrivalInfo.put("routeCode", routeShortName);
                arrivalInfo.put("routeDestination", tripHeadsign);
                arrivalInfo.put("routeName", routeLongName);
                arrivalInfo.put("arrivalTime", arrivalTimeStr);

                // Calcular quantos minutos faltam para o autocarro chegar (ETA)
                try {
                    LocalTime horaChegada = LocalTime.parse(arrivalTimeStr);
                    long minutosFaltam = java.time.temporal.ChronoUnit.MINUTES.between(agora, horaChegada);
                    arrivalInfo.put("etaMinutes", minutosFaltam > 0 ? minutosFaltam : 0);
                } catch (Exception e) {
                    arrivalInfo.put("etaMinutes", new Random().nextInt(15) + 1); // Fallback se falhar o parse
                }

                arrivals.add(arrivalInfo);
            }
        } catch (Exception e) {
            // Se as tabelas gtfs_ ainda não tiverem dados, devolve uma lista simulada limpa para não crashar o Spring
            System.out.println("Aviso: Tabelas GTFS vazias ou erro na query: " + e.getMessage());
        }

        response.put("arrivals", arrivals);
        return response;
    }

    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    private Map<String, Object> routeToMap(Route route) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", route.getId());
        data.put("code", route.getCode());
        data.put("origin", route.getOrigin());
        data.put("destination", route.getDestination());
        data.put("status", route.getStatus());
        return data;
    }

    private Map<String, Object> routeToArrivalMap(Route route) {
        Map<String, Object> data = new HashMap<>();
        data.put("routeCode", route.getCode());
        data.put("routeDestination", route.getDestination());
        data.put("routeName", buildRouteName(route));
        data.put("arrivalTime", null);
        data.put("etaMinutes", null);
        data.put("source", "assignedRoute");
        return data;
    }

    private String buildRouteName(Route route) {
        String origin = route.getOrigin();
        String destination = route.getDestination();
        if (origin != null && !origin.isBlank() && destination != null && !destination.isBlank()) {
            return origin + " -> " + destination;
        }
        if (origin != null && !origin.isBlank()) {
            return origin;
        }
        if (destination != null && !destination.isBlank()) {
            return destination;
        }
        return "Rota TUB";
    }

    private Optional<Route> findRouteForStop(Stop stop) {
        List<Route> activeRoutes = routeRepository.findByStatus("ACTIVE");
        Optional<Route> route = activeRoutes.stream()
                .filter(candidate -> routeHasStop(candidate, stop))
                .findFirst();

        if (route.isPresent()) {
            return route;
        }

        return routeRepository.findAll().stream()
                .filter(candidate -> routeHasStop(candidate, stop))
                .findFirst();
    }

    private boolean routeHasStop(Route route, Stop stop) {
        if (route.getStops() == null || route.getStops().isEmpty()) {
            return false;
        }

        String stopId = stop.getId() == null ? "" : String.valueOf(stop.getId());
        String stopName = stop.getName() == null ? "" : stop.getName().trim().toLowerCase();

        return route.getStops().stream()
                .filter(Objects::nonNull)
                .flatMap(value -> Arrays.stream(value.split("[,;]+")))
                .map(String::trim)
                .anyMatch(token -> {
                    String normalized = token.toLowerCase();
                    return normalized.equals(stopName)
                            || token.equals(stopId)
                            || (!stopName.isBlank() && normalized.contains(stopName))
                            || (!stopName.isBlank() && !normalized.isBlank() && stopName.contains(normalized));
                });
    }
}

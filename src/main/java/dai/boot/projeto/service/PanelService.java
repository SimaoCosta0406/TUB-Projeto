package dai.boot.projeto.service;

import dai.boot.projeto.entities.InformationPanel;
import dai.boot.projeto.entities.Route;
import dai.boot.projeto.entities.Stop;
import dai.boot.projeto.repository.InformationPanelRepository;
import dai.boot.projeto.repository.RouteRepository;
import dai.boot.projeto.repository.StopRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
    private EntityManager entityManager;

    public List<InformationPanel> getAllPanels() {
        return panelRepository.findAll();
    }

    public Optional<InformationPanel> getPanelById(Long id) {
        return panelRepository.findById(id);
    }

    public InformationPanel createPanel(String name, Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("Paragem nao encontrada com id: " + stopId));

        InformationPanel panel = new InformationPanel();
        panel.setLocation(name);
        panel.setStop(stop);
        panel.setStatus("ACTIVE");
        panel.setInstallationDate(LocalDateTime.now());
        panel.setLastUpdated(LocalDateTime.now());

        return panelRepository.save(panel);
    }

    public void deletePanel(Long id) {
        panelRepository.deleteById(id);
    }

    public Map<String, Object> getPanelRealTimeInfo(Long panelId) {
        InformationPanel panel = panelRepository.findById(panelId)
                .orElseThrow(() -> new RuntimeException("Painel nao encontrado com id: " + panelId));
        Stop stop = panel.getStop();
        if (stop == null) {
            throw new RuntimeException("Painel nao tem paragem associada");
        }

        Map<String, Object> response = getStopPanelsRealTimeInfo(stop.getId());
        List<Map<String, Object>> arrivals = castArrivals(response.get("arrivals"));
        List<Route> routesAtStop = findRoutesForStop(stop);

        if (arrivals.isEmpty() && !Boolean.TRUE.equals(response.get("scheduleDataAvailable"))) {
            arrivals = routesAtStop.stream()
                    .map(this::routeToArrivalMap)
                    .toList();
            response.put("arrivals", arrivals);
        }

        for (Map<String, Object> arrival : arrivals) {
            addPassengerCountsToArrival(arrival);
        }

        response.put("panelId", panelId);
        response.put("passengerCounts", passengerService.getCurrentCountForPanel(panelId));
        response.put("routesAtStop", routesAtStop.stream().map(this::routeToMap).toList());
        return response;
    }

    public Map<String, Object> getStopPanelsRealTimeInfo(Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new RuntimeException("Paragem nao encontrada"));

        Map<String, Object> response = new HashMap<>();
        response.put("stopId", stop.getId());
        response.put("stopName", stop.getName());
        response.put("lastUpdate", LocalDateTime.now().toString());

        String sql = "SELECT r.route_short_name, r.route_long_name, t.trip_headsign, st.arrival_time " +
                "FROM gtfs_stop_times st " +
                "JOIN gtfs_trips t ON st.trip_id = t.trip_id " +
                "JOIN gtfs_routes r ON t.route_id = r.route_id " +
                "WHERE st.stop_id = :stopId " +
                "ORDER BY st.arrival_time ASC LIMIT 20";

        Map<String, Map<String, Object>> nextArrivalByRoute = new LinkedHashMap<>();
        boolean scheduleDataAvailable = false;

        try {
            List<Object[]> rows = entityManager.createNativeQuery(sql)
                    .setParameter("stopId", String.valueOf(stopId))
                    .getResultList();
            scheduleDataAvailable = !rows.isEmpty();

            LocalTime now = LocalTime.now();

            for (Object[] row : rows) {
                String routeShortName = (String) row[0];
                String routeLongName = (String) row[1];
                String tripHeadsign = (String) row[2];
                String arrivalTime = (String) row[3];
                Integer etaMinutes = calculateEtaMinutes(now, arrivalTime);

                if (etaMinutes == null || etaMinutes < 0 || etaMinutes > 15) {
                    continue;
                }

                Map<String, Object> arrival = new HashMap<>();
                arrival.put("routeCode", routeShortName);
                arrival.put("routeDestination", tripHeadsign);
                arrival.put("routeName", routeLongName);
                arrival.put("arrivalTime", arrivalTime);
                arrival.put("etaMinutes", etaMinutes);
                arrival.put("source", "schedule");

                nextArrivalByRoute.putIfAbsent(normalize(routeShortName), arrival);
            }
        } catch (Exception e) {
            System.out.println("Aviso: tabelas GTFS vazias ou erro na query: " + e.getMessage());
        }

        response.put("arrivals", new ArrayList<>(nextArrivalByRoute.values()));
        response.put("scheduleDataAvailable", scheduleDataAvailable);
        return response;
    }

    public List<Stop> getAllStops() {
        return stopRepository.findAll();
    }

    private void addPassengerCountsToArrival(Map<String, Object> arrival) {
        String routeCode = arrival.get("routeCode") == null ? null : arrival.get("routeCode").toString();
        Map<String, Object> passengerCounts = passengerService.getCurrentCountForLine(routeCode);
        arrival.put("passengerCounts", passengerCounts);
        arrival.put("currentOccupancy", passengerCounts.get("currentOccupancy"));
        arrival.put("capacity", PassengerService.MAX_BUS_OCCUPANCY);
    }

    private Integer calculateEtaMinutes(LocalTime now, String arrivalTime) {
        try {
            String[] parts = arrivalTime.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int second = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            if (hour >= 24) {
                return null;
            }
            return (int) Duration.between(now, LocalTime.of(hour, minute, second)).toMinutes();
        } catch (Exception e) {
            return null;
        }
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
        data.put("source", "routeAtStop");
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

    private List<Route> findRoutesForStop(Stop stop) {
        List<Route> routes = routeRepository.findByStatus("ACTIVE").stream()
                .filter(route -> routeHasStop(route, stop))
                .toList();

        if (!routes.isEmpty()) {
            return routes;
        }

        return routeRepository.findAll().stream()
                .filter(route -> routeHasStop(route, stop))
                .toList();
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> castArrivals(Object arrivals) {
        if (arrivals instanceof List<?>) {
            return (List<Map<String, Object>>) arrivals;
        }
        return new ArrayList<>();
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase();
    }
}

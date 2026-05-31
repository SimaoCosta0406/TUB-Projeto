package dai.boot.projeto.service;

import dai.boot.projeto.entities.PassengerCount;
import dai.boot.projeto.repository.PassengerCountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PassengerService {
    public static final int MAX_BUS_OCCUPANCY = 55;

    @Autowired
    private PassengerCountRepository repository;

    public PassengerCount save(PassengerCount data) {
        sanitizeReading(data);
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        if (data.getLine() == null || data.getLine().isBlank()) {
            data.setLine("Painel " + data.getPanelId());
        }
        return repository.save(data);
    }

    public List<PassengerCount> getCounts(Long panelId, LocalDateTime from, LocalDateTime to) {
        return repository.findByPanelIdAndTimestampBetween(panelId, from, to);
    }

    public int calculateOccupancy(Long panelId, LocalDateTime from, LocalDateTime to) {
        List<PassengerCount> counts = repository.findByPanelIdAndTimestampBetween(panelId, from, to);
        return clampOccupancy(counts.stream()
                .mapToInt(c -> c.getEntryCount() - c.getExitCount())
                .sum());
    }

    public Map<String, Object> getSummary(Long panelId) {
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
        List<PassengerCount> data = repository.findByPanelIdAndTimestampBetween(panelId, yesterday, LocalDateTime.now());

        int totalEntries = data.stream().mapToInt(PassengerCount::getEntryCount).sum();
        int totalExits = data.stream().mapToInt(PassengerCount::getExitCount).sum();

        Map<String, Object> summary = new HashMap<>();
        summary.put("panelId", panelId);
        summary.put("totalEntries", totalEntries);
        summary.put("totalExits", totalExits);
        summary.put("currentBalance", clampOccupancy(totalEntries - totalExits));
        return summary;
    }

    public List<PassengerCount> getAllCounts() {
        return repository.findAll();
    }

    public Map<Long, PassengerCount> getLatestCountsPerPanel() {
        List<PassengerCount> allCounts = repository.findAll();
        Map<Long, PassengerCount> latestPerPanel = new HashMap<>();
        for (PassengerCount count : allCounts) {
            PassengerCount existing = latestPerPanel.get(count.getPanelId());
            if (existing == null || isAfter(count.getTimestamp(), existing.getTimestamp())) {
                latestPerPanel.put(count.getPanelId(), count);
            }
        }
        return latestPerPanel;
    }

    public Map<String, Object> getCurrentCountForPanel(Long panelId) {
        int totalEntries = 0;
        int totalExits = 0;
        LocalDateTime latestTimestamp = null;

        for (PassengerCount count : repository.findAll()) {
            if (!panelId.equals(count.getPanelId())) {
                continue;
            }

            totalEntries += count.getEntryCount();
            totalExits += count.getExitCount();

            if (isAfter(count.getTimestamp(), latestTimestamp)) {
                latestTimestamp = count.getTimestamp();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("panelId", panelId);
        result.put("totalEntries", totalEntries);
        result.put("totalExits", totalExits);
        result.put("currentOccupancy", clampOccupancy(totalEntries - totalExits));
        result.put("maxOccupancy", MAX_BUS_OCCUPANCY);
        result.put("latestTimestamp", latestTimestamp);
        return result;
    }

    public Map<String, Object> getCurrentCountForLine(String line) {
        String normalizedLine = normalizeLine(line);
        int totalEntries = 0;
        int totalExits = 0;
        LocalDateTime latestTimestamp = null;

        for (PassengerCount count : repository.findAll()) {
            if (!normalizedLine.equals(normalizeLine(count.getLine()))) {
                continue;
            }

            totalEntries += count.getEntryCount();
            totalExits += count.getExitCount();

            if (isAfter(count.getTimestamp(), latestTimestamp)) {
                latestTimestamp = count.getTimestamp();
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("line", line);
        result.put("totalEntries", totalEntries);
        result.put("totalExits", totalExits);
        result.put("currentOccupancy", clampOccupancy(totalEntries - totalExits));
        result.put("maxOccupancy", MAX_BUS_OCCUPANCY);
        result.put("latestTimestamp", latestTimestamp);
        return result;
    }

    public Map<Long, Map<String, Object>> getCurrentCountsPerPanel() {
        Map<Long, Map<String, Object>> currentCounts = new HashMap<>();

        for (PassengerCount count : repository.findAll()) {
            if (count.getPanelId() == null) {
                continue;
            }

            Map<String, Object> panelCounts = currentCounts.computeIfAbsent(count.getPanelId(), panelId -> {
                Map<String, Object> data = new HashMap<>();
                data.put("panelId", panelId);
                data.put("totalEntries", 0);
                data.put("totalExits", 0);
                data.put("currentOccupancy", 0);
                data.put("maxOccupancy", MAX_BUS_OCCUPANCY);
                data.put("latestTimestamp", null);
                return data;
            });

            int totalEntries = (int) panelCounts.get("totalEntries") + count.getEntryCount();
            int totalExits = (int) panelCounts.get("totalExits") + count.getExitCount();
            panelCounts.put("totalEntries", totalEntries);
            panelCounts.put("totalExits", totalExits);
            panelCounts.put("currentOccupancy", clampOccupancy(totalEntries - totalExits));

            LocalDateTime latestTimestamp = (LocalDateTime) panelCounts.get("latestTimestamp");
            if (isAfter(count.getTimestamp(), latestTimestamp)) {
                panelCounts.put("latestTimestamp", count.getTimestamp());
            }
        }

        return currentCounts;
    }

    public List<PassengerCount> calculateCurrentOccupancy() {
        LocalDateTime recent = LocalDateTime.now().minusHours(1);
        return repository.findByTimestampAfter(recent);
    }

    public PassengerCount simulateEntry(Long panelId, int count) {
        return simulateEntry(panelId, null, count);
    }

    public PassengerCount simulateEntry(Long panelId, String line, int count) {
        int currentOccupancy = getCurrentOccupancy(panelId, line);
        int allowedEntries = Math.min(Math.max(count, 0), MAX_BUS_OCCUPANCY - currentOccupancy);

        PassengerCount record = new PassengerCount();
        record.setPanelId(panelId);
        record.setEntryCount(allowedEntries);
        record.setExitCount(0);
        record.setTimestamp(LocalDateTime.now());
        record.setLine((line == null || line.isBlank()) ? "Simulacao" : line);
        return repository.save(record);
    }

    public PassengerCount simulateExit(Long panelId, int count) {
        return simulateExit(panelId, null, count);
    }

    public PassengerCount simulateExit(Long panelId, String line, int count) {
        int currentOccupancy = getCurrentOccupancy(panelId, line);
        int allowedExits = Math.min(Math.max(count, 0), currentOccupancy);

        PassengerCount record = new PassengerCount();
        record.setPanelId(panelId);
        record.setEntryCount(0);
        record.setExitCount(allowedExits);
        record.setTimestamp(LocalDateTime.now());
        record.setLine((line == null || line.isBlank()) ? "Simulacao" : line);
        return repository.save(record);
    }

    public Map<String, Object> getTotalEntries() {
        List<PassengerCount> all = repository.findAll();
        int totalEntries = all.stream().mapToInt(PassengerCount::getEntryCount).sum();
        int totalExits = all.stream().mapToInt(PassengerCount::getExitCount).sum();

        Map<String, Object> result = new HashMap<>();
        result.put("totalEntries", totalEntries);
        result.put("totalExits", totalExits);
        result.put("netOccupancy", clampOccupancy(totalEntries - totalExits));
        result.put("maxOccupancy", MAX_BUS_OCCUPANCY);
        return result;
    }

    public PassengerCount setCurrentOccupancy(Long panelId, int occupancy) {
        Map<String, Object> current = getCurrentCountForPanel(panelId);
        int totalEntries = (int) current.get("totalEntries");
        int totalExits = (int) current.get("totalExits");
        int currentBalance = totalEntries - totalExits;
        int difference = clampOccupancy(occupancy) - currentBalance;

        PassengerCount record = new PassengerCount();
        record.setPanelId(panelId);
        record.setEntryCount(Math.max(difference, 0));
        record.setExitCount(Math.max(-difference, 0));
        record.setTimestamp(LocalDateTime.now());
        record.setLine("Atualizacao manual");
        return repository.save(record);
    }

    public PassengerCount correctOccupancy(Long panelId, int entries, int exits) {
        return setCurrentOccupancy(panelId, clampOccupancy(entries - exits));
    }

    public PassengerCount resetOccupancy(Long panelId) {
        return setCurrentOccupancy(panelId, 0);
    }

    private boolean isAfter(LocalDateTime candidate, LocalDateTime current) {
        if (candidate == null) {
            return false;
        }
        return current == null || candidate.isAfter(current);
    }

    private int clampOccupancy(int occupancy) {
        return Math.max(0, Math.min(MAX_BUS_OCCUPANCY, occupancy));
    }

    private void sanitizeReading(PassengerCount data) {
        data.setEntryCount(Math.max(0, data.getEntryCount()));
        data.setExitCount(Math.max(0, data.getExitCount()));

        if (data.getPanelId() == null) {
            return;
        }

        int currentOccupancy = getCurrentOccupancy(data.getPanelId(), data.getLine());
        int allowedEntries = Math.min(data.getEntryCount(), MAX_BUS_OCCUPANCY - currentOccupancy);
        int occupancyAfterEntries = currentOccupancy + allowedEntries;
        int allowedExits = Math.min(data.getExitCount(), occupancyAfterEntries);

        data.setEntryCount(allowedEntries);
        data.setExitCount(allowedExits);
    }

    private int getCurrentOccupancy(Long panelId, String line) {
        if (line != null && !line.isBlank()) {
            return (int) getCurrentCountForLine(line).get("currentOccupancy");
        }
        return (int) getCurrentCountForPanel(panelId).get("currentOccupancy");
    }

    private String normalizeLine(String line) {
        if (line == null) {
            return "";
        }
        String normalized = line.trim().toLowerCase()
                .replace("linha", "")
                .replaceAll("[^a-z0-9]", "");
        if (normalized.matches("\\d+")) {
            return String.valueOf(Integer.parseInt(normalized));
        }
        return normalized;
    }
}

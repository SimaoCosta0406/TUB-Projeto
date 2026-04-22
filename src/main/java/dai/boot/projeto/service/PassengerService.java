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
    
    @Autowired
    private PassengerCountRepository repository;

    public PassengerCount save(PassengerCount data){
        return repository.save(data);
    }
    public List<PassengerCount> getCounts(Long panelId, LocalDateTime from, LocalDateTime to) {
        return repository.findByPanelIdAndTimestampBetween(panelId, from, to);
    }

    public int calculateOccupancy(Long panelId, LocalDateTime from, LocalDateTime to) {
        List<PassengerCount> counts = repository.findByPanelIdAndTimestampBetween(panelId, from, to);
        return counts.stream()
                .mapToInt(c -> c.getEntryCount() - c.getExitCount())
                .sum();
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
        summary.put("currentBalance", totalEntries - totalExits);
        
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
            if (existing == null || count.getTimestamp().isAfter(existing.getTimestamp())) {
                latestPerPanel.put(count.getPanelId(), count);
            }
        }
        return latestPerPanel;
    }

    public List<PassengerCount> calculateCurrentOccupancy() {
        // Assuming this returns the latest counts per panel or something
        // For simplicity, return all recent counts
        LocalDateTime recent = LocalDateTime.now().minusHours(1);
        return repository.findByTimestampAfter(recent);
    }

    // Simular entrada de passageiro
    public PassengerCount simulateEntry(Long panelId, int count) {
        PassengerCount record = new PassengerCount();
        record.setPanelId(panelId);
        record.setEntryCount(count);
        record.setExitCount(0);
        record.setTimestamp(LocalDateTime.now());
        record.setLine("Simulação");
        return repository.save(record);
    }

    // Simular saída de passageiro
    public PassengerCount simulateExit(Long panelId, int count) {
        PassengerCount record = new PassengerCount();
        record.setPanelId(panelId);
        record.setEntryCount(0);
        record.setExitCount(count);
        record.setTimestamp(LocalDateTime.now());
        record.setLine("Simulação");
        return repository.save(record);
    }

    // Total de entradas de todos os painéis
    public Map<String, Object> getTotalEntries() {
        List<PassengerCount> all = repository.findAll();
        int totalEntries = all.stream().mapToInt(PassengerCount::getEntryCount).sum();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalEntries", totalEntries);
        result.put("totalExits", all.stream().mapToInt(PassengerCount::getExitCount).sum());
        result.put("netOccupancy", totalEntries - all.stream().mapToInt(PassengerCount::getExitCount).sum());
        
        return result;
    }
}


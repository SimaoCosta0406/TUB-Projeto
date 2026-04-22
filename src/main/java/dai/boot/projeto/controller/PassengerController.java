package dai.boot.projeto.controller;

import dai.boot.projeto.entities.PassengerCount;
import dai.boot.projeto.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    @Autowired
    private PassengerService passengerService;

    
    @PostMapping
    public PassengerCount createReading(@RequestBody PassengerCount data) {
        return passengerService.save(data);
    }

    
    @GetMapping
    public List<PassengerCount> listCounts(
            @RequestParam Long panelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return passengerService.getCounts(panelId, from, to);
    }

   
    @GetMapping("/{panelId}/occupancy")
    public int getOccupancy(
            @PathVariable Long panelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return passengerService.calculateOccupancy(panelId, from, to);
    }

    
    @GetMapping("/{panelId}/summary")
    public Map<String, Object> getSummary(@PathVariable Long panelId) {
        return passengerService.getSummary(panelId);
    }
   
    @GetMapping("/all")
    public List<PassengerCount> getAll() {
        return passengerService.getAllCounts();
    }

    @GetMapping("/latest")
    public Map<Long, PassengerCount> getLatestCounts() {
        return passengerService.getLatestCountsPerPanel();
    }

    // UC6 - Passo 5: O sistema apresenta a ocupação atual [cite: 25]
    @GetMapping("/live-status")
    public ResponseEntity<List<PassengerCount>> getLiveOccupancy() {
        // Retorna a lista de veículos com a ocupação calculada mais recente
        return ResponseEntity.ok(passengerService.calculateCurrentOccupancy());
    }

    // Simulação de entrada
    @PostMapping("/simulate/entry")
    public ResponseEntity<PassengerCount> simulateEntry(
            @RequestParam Long panelId,
            @RequestParam(defaultValue = "1") int count) {
        PassengerCount record = passengerService.simulateEntry(panelId, count);
        return ResponseEntity.ok(record);
    }

    // Simulação de saída
    @PostMapping("/simulate/exit")
    public ResponseEntity<PassengerCount> simulateExit(
            @RequestParam Long panelId,
            @RequestParam(defaultValue = "1") int count) {
        PassengerCount record = passengerService.simulateExit(panelId, count);
        return ResponseEntity.ok(record);
    }

    // Total de entradas (soma de todas as entradas)
    @GetMapping("/total-entries")
    public ResponseEntity<Map<String, Object>> getTotalEntries() {
        return ResponseEntity.ok(passengerService.getTotalEntries());
    }

    // Corrigir ocupação de um painel manualmente
    @PostMapping("/{panelId}/correct")
    public ResponseEntity<Map<String, String>> correctOccupancy(
            @PathVariable Long panelId,
            @RequestParam int entries,
            @RequestParam int exits) {
        passengerService.correctOccupancy(panelId, entries, exits);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ocupação corrigida com sucesso");
        response.put("panelId", panelId.toString());
        return ResponseEntity.ok(response);
    }

    // Resetar ocupação de um painel
    @PostMapping("/{panelId}/reset")
    public ResponseEntity<Map<String, String>> resetOccupancy(@PathVariable Long panelId) {
        passengerService.resetOccupancy(panelId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Ocupação resetada com sucesso");
        response.put("panelId", panelId.toString());
        return ResponseEntity.ok(response);
    }
}




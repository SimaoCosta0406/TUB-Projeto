package dai.boot.projeto.controller;

import dai.boot.projeto.entities.PassengerCount;
import dai.boot.projeto.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
}



package dai.boot.projeto.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "passenger_counts")
public class PassengerCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Campos originais
    private Long panelId;
    private String line;
    private LocalDateTime timestamp;
    private int entryCount;
    private int exitCount;
    private int occupancy;

    // Campos adicionados para o DataIngestionService
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "stop_id")
    private Stop stop;

    private Double occupancyPercentage;

    // ── CONSTRUTORES ──────────────────────────────────────

    public PassengerCount() {}

    public PassengerCount(Long id, Long panelId, String line, LocalDateTime timestamp,
                          int entryCount, int exitCount, int occupancy) {
        this.id = id;
        this.panelId = panelId;
        this.line = line;
        this.timestamp = timestamp;
        this.entryCount = entryCount;
        this.exitCount = exitCount;
        this.occupancy = occupancy;
    }

    @PrePersist
    @PreUpdate
    public void calculateOccupancyValue() {
        this.occupancy = this.entryCount - this.exitCount;
    }

    // ── GETTERS & SETTERS ─────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPanelId() { return panelId; }
    public void setPanelId(Long panelId) { this.panelId = panelId; }

    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public int getEntryCount() { return entryCount; }
    public void setEntryCount(int entryCount) { this.entryCount = entryCount; }

    public int getExitCount() { return exitCount; }
    public void setExitCount(int exitCount) { this.exitCount = exitCount; }

    public int getOccupancy() { return occupancy; }
    public void setOccupancy(int occupancy) { this.occupancy = occupancy; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }

    public Double getOccupancyPercentage() { return occupancyPercentage; }
    public void setOccupancyPercentage(Double occupancyPercentage) { this.occupancyPercentage = occupancyPercentage; }
}
package dai.boot.projeto.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "routes")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String origin;
    private String destination;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "route_stops", joinColumns = @JoinColumn(name = "route_id"))
    @Column(name = "stop")
    private List<String> stops = new ArrayList<>();

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(columnDefinition = "text")
    private String metadata;

    // -------- CONSTRUTORES --------

    public Route() {}

    public Route(String code, String origin, String destination) {
        this.code = code;
        this.origin = origin;
        this.destination = destination;
    }

    public Route(String code, String origin, String destination, List<String> stops, String status, String metadata) {
        this.code = code;
        this.origin = origin;
        this.destination = destination;
        if (stops != null)  this.stops = stops;
        if (status != null) this.status = status;
        this.metadata = metadata;
    }

    // -------- GETTERS E SETTERS --------

    public Long getId()                  { return id; }
    public void setId(Long id)           { this.id = id; }

    public String getCode()              { return code; }
    public void setCode(String code)     { this.code = code; }

    public String getOrigin()            { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination()                   { return destination; }
    public void setDestination(String destination)   { this.destination = destination; }

    public List<String> getStops()               { return stops; }
    public void setStops(List<String> stops)     { this.stops = stops; }

    public String getStatus()                { return status; }
    public void setStatus(String status)     { this.status = status; }

    public String getMetadata()              { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    // -------- MÉTODOS UTILITÁRIOS --------

    public void addStop(String stop) {
        if (this.stops == null) this.stops = new ArrayList<>();
        this.stops.add(stop);
    }

    public boolean removeStop(String stop) {
        return this.stops != null && this.stops.remove(stop);
    }

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.status);
    }

    public String stopsSummary() {
        if (stops == null || stops.isEmpty()) return "";
        return String.join(" -> ", stops);
    }

    // -------- EQUALS, HASHCODE E TOSTRING --------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Route)) return false;
        Route route = (Route) o;
        if (this.id != null && route.id != null) return Objects.equals(this.id, route.id);
        return Objects.equals(this.code, route.code);
    }

    @Override
    public int hashCode() {
        return this.id != null ? Objects.hash(this.id) : Objects.hash(this.code);
    }

    @Override
    public String toString() {
        return "Route{id=" + id + ", code='" + code + "', origin='" + origin +
                "', destination='" + destination + "', stops=" + stops +
                ", status='" + status + "', metadata='" + metadata + "'}";
    }
}
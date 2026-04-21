package dai.boot.projeto.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;
    private String description;
    private String source;
    private String severity;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    // Novo: username do supervisor que aceitou
    private String acceptedBy;

    // Novo: motivo de inconsistência escrito pelo admin
    @Column(columnDefinition = "TEXT")
    private String inconsistentReason;

    public Alert() {
    }

    public Alert(String type, String description, String source, String severity, String status, String metadata) {
        this.type = type;
        this.description = description;
        this.source = source;
        this.severity = severity;
        this.status = status;
        this.metadata = metadata;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }

    public String getAcceptedBy() { return acceptedBy; }
    public void setAcceptedBy(String acceptedBy) { this.acceptedBy = acceptedBy; }

    public String getInconsistentReason() { return inconsistentReason; }
    public void setInconsistentReason(String inconsistentReason) { this.inconsistentReason = inconsistentReason; }
}
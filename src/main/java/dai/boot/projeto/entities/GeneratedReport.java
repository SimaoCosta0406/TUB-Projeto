package dai.boot.projeto.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_reports")
public class GeneratedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reportType;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime generatedAt;

    private Boolean generatedBySystem;

    public GeneratedReport() {
    }

    public Long getId() {
        return id;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public Boolean getGeneratedBySystem() {
        return generatedBySystem;
    }

    public void setGeneratedBySystem(Boolean generatedBySystem) {
        this.generatedBySystem = generatedBySystem;
    }
}
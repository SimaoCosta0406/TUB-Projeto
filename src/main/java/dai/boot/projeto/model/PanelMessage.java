package dai.boot.projeto.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "panel_message")
public class PanelMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String content;
    private Integer priority;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String status;

    @Column(name = "target_zone")
    private String targetZone;

    @Column(name = "message_type")
    private String messageType;

    @ManyToOne
    @JoinColumn(name = "panel_id")
    private InformationPanel panel;

    @ManyToOne
    @JoinColumn(name = "created_by_id")
    private Operator createdBy;

    public PanelMessage() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTargetZone() { return targetZone; }
    public void setTargetZone(String targetZone) { this.targetZone = targetZone; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public InformationPanel getPanel() { return panel; }
    public void setPanel(InformationPanel panel) { this.panel = panel; }

    public Operator getCreatedBy() { return createdBy; }
    public void setCreatedBy(Operator createdBy) { this.createdBy = createdBy; }
}
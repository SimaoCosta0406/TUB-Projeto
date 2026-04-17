package dai.boot.projeto.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "information_panel")
public class InformationPanel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;
    private String address;

    @Column(name = "installation_date")
    private LocalDateTime installationDate;

    private String manufacturer;

    @Column(name = "panel_type")
    private String panelType;

    private String status;

    @Column(name = "connectivity_type")
    private String connectivityType;

    @ManyToOne
    @JoinColumn(name = "stop_id")
    private Stop stop;

    public InformationPanel() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDateTime getInstallationDate() { return installationDate; }
    public void setInstallationDate(LocalDateTime installationDate) { this.installationDate = installationDate; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getPanelType() { return panelType; }
    public void setPanelType(String panelType) { this.panelType = panelType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getConnectivityType() { return connectivityType; }
    public void setConnectivityType(String connectivityType) { this.connectivityType = connectivityType; }

    public Stop getStop() { return stop; }
    public void setStop(Stop stop) { this.stop = stop; }
}
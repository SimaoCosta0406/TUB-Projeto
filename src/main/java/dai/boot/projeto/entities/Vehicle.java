package main.java.dai.boot.projeto.entities;

import jakarta.persistence.*;

import java.lang.annotation.Inherited;
import java.util.Objects;

import javax.annotation.processing.Generated;

@Entity
@Table(name = "vehicles")

public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String plate;
    private String model;

    @Column(nullable = false)
    private String status = "IN_SERVICE";

    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private Route route;

    @Column(columnDefinition = "text")
    private String metadata;

    // -------- CONSTRUTORES --------

    public Vehicle(){}

    public Vehicle(String plate, String model, Integer capacity){
        this.plate = plate;
        this.model = model;
        this.capacity = capacity;
    }

    public Vehicle(String plate, String model, String status, Integer capacity, Route route, String metadata){
        this.plate = plate;
        this.model = model;
        if(status != null){
            this.status = status;
        }
        this.capacity = capacity;
        this.route = route;
        this.metadata = metadata;
    }

    // -------- GETTERS E SETTERS --------

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getPlate(){
        return plate;
    }

    public void setPlate(String plate){
        this.plate = plate;
    }

    public String getModel(){
        return model;
    }

    public void setModel(String model){
        this.model = model;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public Integer getCapacity(){
        return capacity;
    }

    public void setCapacity(Integer capacity){
        this.capacity = capacity;
    }

    public Route getRoute(){
        return route;
    }

    public void setRoute(Route route){
        this.route = route;
    }

    public String getMetadata(){
        return metadata;
    }

    public void setMetadata(String metadata){
        this.metadata = metadata;
    }

    // -------- MÉTODOS AUXILIARES --------

    public boolean isInService(){
        return "IN_SERVICE".equalsIgnoreCase(this.status);
    }

    public void markOutOfService(){
        this.status = "OUT_OF_SERVICE";
    }

    public void assignRoute(Route route){
        this.route = route;
    }

    public void unassignRoute(){
        this.route = null;
    }

    //---------EQUALS, HASHCODE E TOSTRING --------

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof Vehicle)) return false;
        Vehicle vehicle = (Vehicle) o;
        if(this.id != null && vehicle.id != null){
            return Objects.equals(this.id, vehicle.id);
        }
        return Objects.equals(this.plate, vehicle.plate);
    }

    @Override
    public int hashCode(){
        if (this.id != null){
            return Objects.hash(this.id);
        }
        return Objects.hash(id, plate);
    }

    @Override
    public String toString(){
        return "Vehicle{" +
                "id=" + id +
                ", plate='" + plate + '\'' +
                ", model='" + model + '\'' +
                ", status='" + status + '\'' +
                ", capacity=" + capacity +
                ", route=" + (route != null ? route.getCode() : "null") +
                ", metadata='" + metadata + '\'' +
                '}';
    }
}

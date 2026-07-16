package com.Uber.entity;


import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("driver_profiles") // Represents the table in the PostgreSQL database
public class DriverProfile {

    @Id
    private Long id;

    private String name;
    private String licensePlate;
    private String homeCity;

    // Standard Getters and Setters (or use @Data if you have Lombok installed)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public String getHomeCity() { return homeCity; }
    public void setHomeCity(String homeCity) { this.homeCity = homeCity; }
}

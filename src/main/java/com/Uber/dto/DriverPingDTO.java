package com.Uber.dto;

public class DriverPingDTO {
    private String driverId;
    private double latitude;
    private double longitude;
    private String city; // Used for dynamic sharding (e.g., "nyc", "london")

    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
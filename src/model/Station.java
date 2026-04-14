package model;

/**
 * Represents a PC or VIP gaming station.
 * Maps to the `stations` table in the database.
 */
public class Station {

    private int    stationId;
    private String stationName;
    private String stationType;   // "regular" or "vip"
    private double ratePerHour;
    private String status;        // "available", "occupied", "maintenance"

    // Constructors
    public Station() {}

    public Station(int stationId, String stationName, String stationType,
                   double ratePerHour, String status) {
        this.stationId   = stationId;
        this.stationName = stationName;
        this.stationType = stationType;
        this.ratePerHour = ratePerHour;
        this.status      = status;
    }

    // Getters
    public int getStationId() {
        return stationId;
    }

    public String getStationName() {
        return stationName;
    }

    public String getStationType() {
        return stationType;
    }

    public double getRatePerHour() {
        return ratePerHour;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public void setStationType(String stationType) {
        this.stationType = stationType;
    }

    public void setRatePerHour(double ratePerHour) {
        this.ratePerHour = ratePerHour;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Convenience
    public boolean isVip() {
        return "vip".equalsIgnoreCase(stationType);
    }

    public boolean isAvailable() {
        return "available".equalsIgnoreCase(status);
    }

    public boolean isOccupied() {
        return "occupied".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Station{stationId=" + stationId
             + ", stationName='" + stationName + "'"
             + ", stationType='" + stationType + "'"
             + ", status='" + status + "'}";
    }
}
package model;

public class Station {
    private int stationId;
    private String stationName;
    private String stationType;
    private double ratePerHour;
    private String status;

    public Station() {}

    public Station(int stationId, String stationName, String stationType,
                   double ratePerHour, String status) {
        this.stationId = stationId; this.stationName = stationName;
        this.stationType = stationType; this.ratePerHour = ratePerHour;
        this.status = status;
    }

    public int getStationId()       { return stationId; }
    public String getStationName()  { return stationName; }
    public String getStationType()  { return stationType; }
    public double getRatePerHour()  { return ratePerHour; }
    public String getStatus()       { return status; }

    public void setStationId(int v)        { this.stationId = v; }
    public void setStationName(String v)   { this.stationName = v; }
    public void setStationType(String v)   { this.stationType = v; }
    public void setRatePerHour(double v)   { this.ratePerHour = v; }
    public void setStatus(String v)        { this.status = v; }

    public boolean isVip()       { return "vip".equalsIgnoreCase(stationType); }
    public boolean isAvailable() { return "available".equalsIgnoreCase(status); }
    public boolean isOccupied()  { return "occupied".equalsIgnoreCase(status); }

    @Override
    public String toString() {
        return "Station{stationId=" + stationId + ", stationName='" + stationName + "', stationType='" + stationType + "', status='" + status + "'}";
    }
}
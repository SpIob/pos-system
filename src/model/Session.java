package model;

import java.sql.Timestamp;

public class Session {
    private int sessionId;
    private int stationId;
    private int userId;
    private Timestamp startTime;
    private Timestamp endTime;
    private int durationMinutes;
    private double sessionCharge;
    private String status;

    public Session() {}

    public Session(int sessionId, int stationId, int userId, Timestamp startTime,
                   Timestamp endTime, int durationMinutes, double sessionCharge,
                   String status) {
        this.sessionId = sessionId; this.stationId = stationId; this.userId = userId;
        this.startTime = startTime; this.endTime = endTime;
        this.durationMinutes = durationMinutes; this.sessionCharge = sessionCharge;
        this.status = status;
    }

    public int getSessionId()           { return sessionId; }
    public int getStationId()           { return stationId; }
    public int getUserId()              { return userId; }
    public Timestamp getStartTime()     { return startTime; }
    public Timestamp getEndTime()       { return endTime; }
    public int getDurationMinutes()     { return durationMinutes; }
    public double getSessionCharge()    { return sessionCharge; }
    public String getStatus()           { return status; }

    public void setSessionId(int v)          { this.sessionId = v; }
    public void setStationId(int v)          { this.stationId = v; }
    public void setUserId(int v)             { this.userId = v; }
    public void setStartTime(Timestamp v)    { this.startTime = v; }
    public void setEndTime(Timestamp v)      { this.endTime = v; }
    public void setDurationMinutes(int v)    { this.durationMinutes = v; }
    public void setSessionCharge(double v)   { this.sessionCharge = v; }
    public void setStatus(String v)          { this.status = v; }

    public boolean isActive() { return "active".equalsIgnoreCase(status); }

    public double calculateCharge(double ratePerHour) {
        return (durationMinutes / 60.0) * ratePerHour;
    }

    @Override
    public String toString() {
        return "Session{sessionId=" + sessionId + ", stationId=" + stationId + ", status='" + status + "', startTime=" + startTime + "}";
    }
}
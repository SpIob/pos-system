package model;

import java.sql.Timestamp;

/**
 * Represents one active or completed PC usage session.
 * Maps to the `sessions` table in the database.
 */
public class Session {

    private int       sessionId;
    private int       stationId;
    private int       userId;
    private Timestamp startTime;
    private Timestamp endTime;         // null if session still active
    private int       durationMinutes;
    private double    sessionCharge;
    private String    status;          // "active", "completed", "cancelled"

    // Constructors
    public Session() {}

    public Session(int sessionId, int stationId, int userId,
                   Timestamp startTime, Timestamp endTime,
                   int durationMinutes, double sessionCharge,
                   String status) {
        this.sessionId       = sessionId;
        this.stationId       = stationId;
        this.userId          = userId;
        this.startTime       = startTime;
        this.endTime         = endTime;
        this.durationMinutes = durationMinutes;
        this.sessionCharge   = sessionCharge;
        this.status          = status;
    }

    // Getters
    public int getSessionId() {
        return sessionId;
    }

    public int getStationId() {
        return stationId;
    }

    public int getUserId() {
        return userId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public double getSessionCharge() {
        return sessionCharge;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public void setSessionCharge(double sessionCharge) {
        this.sessionCharge = sessionCharge;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Convenience
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    /**
     * Calculates the charge based on elapsed minutes and rate per hour.
     * Use this when ending a session before saving to the database.
     */
    public double calculateCharge(double ratePerHour) {
        return (durationMinutes / 60.0) * ratePerHour;
    }

    @Override
    public String toString() {
        return "Session{sessionId=" + sessionId
             + ", stationId=" + stationId
             + ", status='" + status + "'"
             + ", startTime=" + startTime + "}";
    }
}
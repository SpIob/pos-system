package dao;

import database.DBConnection;
import model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * SessionDAO.java
 * Handles all database operations for the sessions table.
 */
public class SessionDAO {

    // ---------------------------------------------------------------
    // Start a new session — called when "Start Session" is clicked
    // Returns the new session_id, or -1 on failure
    // ---------------------------------------------------------------
    public int startSession(int stationId, int userId) {
        String sql = "INSERT INTO sessions "
                   + "(station_id, user_id, start_time, status) "
                   + "VALUES (?, ?, NOW(), 'active')";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return -1;

        try (PreparedStatement stmt = conn.prepareStatement(
                sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, stationId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("[SessionDAO] startSession() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return -1;
    }

    // ---------------------------------------------------------------
    // End an active session — called when "End Session" is clicked
    // Calculates duration and charge, then marks session as completed
    // ---------------------------------------------------------------
    public boolean endSession(int sessionId, double ratePerHour) {
        // Step 1: fetch the start time
        String selectSql = "SELECT start_time FROM sessions "
                         + "WHERE session_id = ? AND status = 'active'";

        // Step 2: update with calculated values
        String updateSql = "UPDATE sessions SET "
                         + "end_time = NOW(), "
                         + "duration_minutes = TIMESTAMPDIFF(MINUTE, start_time, NOW()), "
                         + "session_charge = ROUND("
                         + "  (TIMESTAMPDIFF(MINUTE, start_time, NOW()) / 60.0) * ?, 2), "
                         + "status = 'completed' "
                         + "WHERE session_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
            updateStmt.setDouble(1, ratePerHour);
            updateStmt.setInt(2, sessionId);
            return updateStmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[SessionDAO] endSession() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return false;
    }

    // ---------------------------------------------------------------
    // Get all currently active sessions
    // Used by the Dashboard "Active Sessions" KPI card
    // ---------------------------------------------------------------
    public List<Session> getActiveSessions() {
        List<Session> sessions = new ArrayList<>();

        String sql = "SELECT session_id, station_id, user_id, start_time, "
                   + "end_time, duration_minutes, session_charge, status "
                   + "FROM sessions WHERE status = 'active' "
                   + "ORDER BY start_time ASC";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return sessions;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sessions.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[SessionDAO] getActiveSessions() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return sessions;
    }

    // ---------------------------------------------------------------
    // Get the active session for a specific station
    // Used by StationsPanel to load the correct timer start time
    // ---------------------------------------------------------------
    public Session getActiveSessionByStation(int stationId) {
        String sql = "SELECT session_id, station_id, user_id, start_time, "
                   + "end_time, duration_minutes, session_charge, status "
                   + "FROM sessions "
                   + "WHERE station_id = ? AND status = 'active' "
                   + "LIMIT 1";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            System.err.println("[SessionDAO] getActiveSessionByStation() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return null;
    }

    // Count of currently active sessions (used by Dashboard KPI)   
    public int getActiveSessionCount() {
        String sql = "SELECT COUNT(*) FROM sessions WHERE status = 'active'";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[SessionDAO] getActiveSessionCount() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return 0;
    }

    // Map a ResultSet row to a Session object
    private Session mapRow(ResultSet rs) throws SQLException {
        return new Session(
            rs.getInt("session_id"),
            rs.getInt("station_id"),
            rs.getInt("user_id"),
            rs.getTimestamp("start_time"),
            rs.getTimestamp("end_time"),
            rs.getInt("duration_minutes"),
            rs.getDouble("session_charge"),
            rs.getString("status")
        );
    }
}
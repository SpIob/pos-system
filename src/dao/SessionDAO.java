package dao;

import database.DBConnection;
import model.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SessionDAO {

    public int startSession(int stationId, int userId) {
        String sql = "INSERT INTO sessions (station_id, user_id, start_time, status) "
                   + "VALUES (?, ?, ?, 'active')";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return -1;
        try (PreparedStatement stmt = conn.prepareStatement(sql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, stationId);
            stmt.setInt(2, userId);
            stmt.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean endSession(int sessionId, double ratePerHour) {
        String sql = "UPDATE sessions SET end_time = NOW(), "
                   + "duration_minutes = TIMESTAMPDIFF(MINUTE, start_time, NOW()), "
                   + "session_charge = ROUND((TIMESTAMPDIFF(MINUTE, start_time, NOW()) / 60.0) * ?, 2), "
                   + "status = 'completed' WHERE session_id = ?";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, ratePerHour);
            stmt.setInt(2, sessionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    public List<Session> getActiveSessions() {
        List<Session> sessions = new ArrayList<>();
        String sql = "SELECT session_id, station_id, user_id, start_time, end_time, "
                   + "duration_minutes, session_charge, status FROM sessions "
                   + "WHERE status = 'active' ORDER BY start_time ASC";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return sessions;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) sessions.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return sessions;
    }

    public Session getActiveSessionByStation(int stationId) {
        String sql = "SELECT session_id, station_id, user_id, start_time, end_time, "
                   + "duration_minutes, session_charge, status FROM sessions "
                   + "WHERE station_id = ? AND status = 'active' LIMIT 1";
        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return null;
    }

    public int getActiveSessionCount() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM sessions WHERE status = 'active'");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return 0;
    }

    private Session mapRow(ResultSet rs) throws SQLException {
        return new Session(rs.getInt("session_id"), rs.getInt("station_id"),
            rs.getInt("user_id"), rs.getTimestamp("start_time"),
            rs.getTimestamp("end_time"), rs.getInt("duration_minutes"),
            rs.getDouble("session_charge"), rs.getString("status"));
    }
}
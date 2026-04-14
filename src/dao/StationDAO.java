package dao;

import database.DBConnection;
import model.Station;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * StationDAO.java
 * Handles all database operations for the stations table.
 */
public class StationDAO {

    // Get all stations (used by StationsPanel and dropdowns)
    public List<Station> getAllStations() {
        List<Station> stations = new ArrayList<>();

        String sql = "SELECT station_id, station_name, station_type, "
                   + "rate_per_hour, status "
                   + "FROM stations ORDER BY station_id ASC";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return stations;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StationDAO] getAllStations() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return stations;
    }

    // Get only available stations (used by the Link to Station dropdown)
    public List<Station> getAvailableStations() {
        List<Station> stations = new ArrayList<>();

        String sql = "SELECT station_id, station_name, station_type, "
                   + "rate_per_hour, status "
                   + "FROM stations WHERE status = 'available' "
                   + "ORDER BY station_id ASC";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return stations;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                stations.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[StationDAO] getAvailableStations() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return stations;
    }

    // Count occupied stations (used by the Dashboard KPI card)
    public int getOccupiedCount() {
        String sql = "SELECT COUNT(*) FROM stations WHERE status = 'occupied'";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            System.err.println("[StationDAO] getOccupiedCount() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return 0;
    }

    // ---------------------------------------------------------------
    // Update a station's status
    // status must be: "available", "occupied", or "maintenance"
    // ---------------------------------------------------------------
    public boolean updateStatus(int stationId, String status) {
        String sql = "UPDATE stations SET status = ? WHERE station_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.toLowerCase());
            stmt.setInt(2, stationId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("[StationDAO] updateStatus() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return false;
    }

    // Map a ResultSet row to a Station object
    private Station mapRow(ResultSet rs) throws SQLException {
        return new Station(
            rs.getInt("station_id"),
            rs.getString("station_name"),
            rs.getString("station_type"),
            rs.getDouble("rate_per_hour"),
            rs.getString("status")
        );
    }
}
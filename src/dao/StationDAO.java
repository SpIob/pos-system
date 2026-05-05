package dao;

import database.DBConnection;
import model.Station;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class StationDAO {

    private static final String SELECT_COLS =
        "SELECT station_id, station_name, station_type, rate_per_hour, status FROM stations";

    public List<Station> getAllStations() {
        return queryStations(SELECT_COLS + " ORDER BY station_id ASC", null);
    }

    public List<Station> getAvailableStations() {
        return queryStations(SELECT_COLS + " WHERE status = 'available' ORDER BY station_id ASC", null);
    }

    private List<Station> queryStations(String sql, String statusFilter) {
        List<Station> stations = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) return stations;
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) stations.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return stations;
    }

    public int getOccupiedCount() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return 0;
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM stations WHERE status = 'occupied'");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return 0;
    }

    public boolean updateStatus(int stationId, String status) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE stations SET status = ? WHERE station_id = ?")) {
            stmt.setString(1, status.toLowerCase());
            stmt.setInt(2, stationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return false;
    }

    private Station mapRow(ResultSet rs) throws SQLException {
        return new Station(rs.getInt("station_id"), rs.getString("station_name"),
            rs.getString("station_type"), rs.getDouble("rate_per_hour"),
            rs.getString("status"));
    }
}
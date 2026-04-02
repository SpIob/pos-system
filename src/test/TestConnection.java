package test;

import database.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * TestConnection.java
 * Temporary file to verify that the Railway MySQL connection works.
 * Run this class directly in NetBeans (right-click → Run File).
 * Delete or disable this file once the connection is confirmed.
 */
public class TestConnection {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("  Internet Café POS — Connection Test   ");
        System.out.println("========================================");

        Connection conn = DBConnection.getConnection();

        if (conn != null) {
            System.out.println("[Y] Successfully connected to Railway MySQL!");
            System.out.println("[Y] Database: railway");
            System.out.println();

            // Test 1: Check if tables exist
            testTableExists(conn, "users");
            testTableExists(conn, "stations");
            testTableExists(conn, "sessions");
            testTableExists(conn, "products");
            testTableExists(conn, "transactions");
            testTableExists(conn, "transaction_items");

            System.out.println();

            // Test 2: Fetch and display users
            testFetchUsers(conn);

            // Test 3: Fetch and display stations
            testFetchStations(conn);

            // Close connection
            DBConnection.closeConnection(conn);

        } else {
            System.out.println("[X] Connection FAILED. See errors above.");
        }

        System.out.println("========================================");
        System.out.println("  Test Complete");
        System.out.println("========================================");
    }

    // ---------------------------------------------------------------
    // Checks if a table exists in the database
    // ---------------------------------------------------------------
    private static void testTableExists(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) FROM information_schema.tables "
                + "WHERE table_schema = 'railway' AND table_name = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("[Y] Table found: " + tableName);
            } else {
                System.out.println("[X] Table MISSING: " + tableName
                        + " — Did you run db-schema.sql?");
            }
        } catch (SQLException e) {
            System.err.println("[!] Error checking table: " + tableName);
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Fetches and prints all users from the users table
    // ---------------------------------------------------------------
    private static void testFetchUsers(Connection conn) {
        System.out.println("--- Users Table ---");
        String sql = "SELECT user_id, username, role, created_at FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.printf("  ID: %d | Username: %-15s | Role: %-10s | Created: %s%n",
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getTimestamp("created_at"));
            }
            if (!hasRows)
                System.out.println("  (No users found)");
        } catch (SQLException e) {
            System.err.println("[!] Error fetching users.");
            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------------
    // Fetches and prints all stations from the stations table
    // ---------------------------------------------------------------
    private static void testFetchStations(Connection conn) {
        System.out.println("--- Stations Table ---");
        String sql = "SELECT station_id, station_name, station_type, rate_per_hour, status FROM stations";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            boolean hasRows = false;
            while (rs.next()) {
                hasRows = true;
                System.out.printf("  ID: %d | Name: %-8s | Type: %-8s | Rate: ₱%.2f/hr | Status: %s%n",
                        rs.getInt("station_id"),
                        rs.getString("station_name"),
                        rs.getString("station_type"),
                        rs.getDouble("rate_per_hour"),
                        rs.getString("status"));
            }
            if (!hasRows)
                System.out.println("  (No stations found)");
        } catch (SQLException e) {
            System.err.println("[!] Error fetching stations.");
            e.printStackTrace();
        }
    }
}
package dao;

import database.DBConnection;
import model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO.java
 * Handles all database operations for the users table.
 * Only this class is allowed to run SQL against the users table.
 */
public class UserDAO {

    // ---------------------------------------------------------------
    // Authenticate — used by LoginFrame
    // Returns the matching User object, or null if login fails
    // ---------------------------------------------------------------
    public User authenticate(String username, String plainPassword) {
        String hashed = sha256(plainPassword);
        if (hashed == null) return null;

        String sql = "SELECT user_id, username, password, role, created_at "
                   + "FROM users "
                   + "WHERE username = ? AND password = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, hashed);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[UserDAO] authenticate() failed.");
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return null; // no match found
    }

    // Find user by ID
    public User findById(int userId) {
        String sql = "SELECT user_id, username, password, role, created_at "
                   + "FROM users WHERE user_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return null;
    }
    
    // ---------------------------------------------------------------
    // Get username by user ID — used to populate the Cashier column
    // in Dashboard and Reports transaction tables
    // ---------------------------------------------------------------
    public String getUsernameById(int userId) {
        String sql = "SELECT username FROM users WHERE user_id = ?";

        Connection conn = DBConnection.getConnection();
        if (conn == null) return "—";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.closeConnection(conn);
        }

        return "—";
    }

    // Map a ResultSet row to a User object
    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt("user_id"),
            rs.getString("username"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getTimestamp("created_at")
        );
    }

    // SHA-256 hash — must match what db-schema.sql uses: SHA2(?, 256)
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
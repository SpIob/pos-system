package dao;

import database.DBConnection;
import model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static final String SELECT_COLS =
        "SELECT user_id, username, password, role, created_at FROM users";

    public User authenticate(String username, String plainPassword) {
        String hashed = sha256(plainPassword);
        if (hashed == null) return null;
        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement(
                SELECT_COLS + " WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, hashed);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return null;
    }

    public User findById(int userId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return null;
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_COLS + " WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) return list;
        try (PreparedStatement stmt = conn.prepareStatement(SELECT_COLS + " ORDER BY user_id ASC");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addUser(String username, String plainPassword, String role) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, username);
            stmt.setString(2, sha256(plainPassword));
            stmt.setString(3, role.toLowerCase());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false; // duplicate username
        }
    }

    public boolean updatePassword(int userId, String newPlainPassword) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return false;
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET password = ? WHERE user_id = ?")) {
            stmt.setString(1, sha256(newPlainPassword));
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getUsernameById(int userId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) return "—";
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT username FROM users WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { DBConnection.closeConnection(conn); }
        return "—";
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(rs.getInt("user_id"), rs.getString("username"),
            rs.getString("password"), rs.getString("role"), rs.getTimestamp("created_at"));
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
}